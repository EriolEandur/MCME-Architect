/*
 * Copyright (C) 2016 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.architect.specialBlockHandling.listener;

import com.mcmiddleearth.architect.Modules;
import com.mcmiddleearth.architect.Permission;
import com.mcmiddleearth.architect.PluginData;
import com.mcmiddleearth.architect.blockData.BlockDataManager;
import com.mcmiddleearth.architect.blockData.attributes.Attribute;
import com.mcmiddleearth.architect.chunkUpdate.ChunkUpdateUtil;
import com.mcmiddleearth.architect.specialBlockHandling.data.SpecialBlockInventoryData;
import com.mcmiddleearth.architect.specialBlockHandling.specialBlocks.SpecialBlockItemBlock;
import com.mcmiddleearth.pluginutil.EventUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Eriol_Eandur
 */
public class BlockCycleListener implements Listener {

    private final Map<Player, BlockDataManager> playerBlockDataManager = new HashMap<>();
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    private void cycleAttribute(PlayerInteractEvent event) {
        if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                && EventUtil.isMainHandEvent(event)
                && event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.STICK)) {
            event.setCancelled(true);
            Player p = event.getPlayer();
            if(!PluginData.isModuleEnabled(p.getWorld(),Modules.CYCLE_BLOCKS)) {
                sendNotEnabledErrorMessage(p);
                return;
            }   
            Block block = event.getClickedBlock();
            if(!PluginData.checkBuildPermissions(p,block.getLocation(),Permission.CYCLE_BLOCKS)) {
                return;
            }
            
            BlockDataManager blockDataManager = getOrCreateBlockDataManager(p);
            blockDataManager.nextAttribute(block.getBlockData());
            Attribute attrib = blockDataManager.getAttribute(block.getBlockData());
            PluginData.getMessageUtil().sendInfoMessage(p, "Selected Block State: "
                                          +(attrib!=null?attrib.getName()+" : "+attrib.getState():"null"));
            ChunkUpdateUtil.sendUpdates(block, p);
        }
    }
        
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void CycleState(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
              && p.getInventory().getItemInMainHand().getType().equals(Material.STICK)
              && EventUtil.isMainHandEvent(event)) {
            if(!PluginData.isModuleEnabled(p.getWorld(),Modules.CYCLE_BLOCKS)) {
                sendNotEnabledErrorMessage(p);
                return;
            }   
            event.setCancelled(true);
            Block block = event.getClickedBlock();
            if(!PluginData.checkBuildPermissions(p,block.getLocation(),
                                                 Permission.CYCLE_BLOCKS)) {
                return;
            }
            ArmorStand armorStand = SpecialBlockItemBlock.getArmorStand(event.getClickedBlock().getLocation());
            if(armorStand!=null) {
                cycleItemBlock(armorStand);
            } else {
                BlockData data = block.getBlockData();
                BlockDataManager blockDataManager = getOrCreateBlockDataManager(p);
                Attribute attrib = blockDataManager.getAttribute(data);
                if(attrib!=null) {
                    attrib.cycleState();
                    block.setBlockData(data,false);
                    PluginData.getMessageUtil().sendInfoMessage(p,"Set Block State: "
                                                                + attrib.getName()+" : "+attrib.getState());
                }
            }
        }
    }

    private void cycleItemBlock(ArmorStand armorStand) {
        if(armorStand.getCustomName() != null 
                && armorStand.getCustomName().startsWith(SpecialBlockItemBlock.PREFIX)) {
            ItemStack item = armorStand.getHelmet();
            //item.setDurability((short)((item.getDurability()+1)%Short.MAX_VALUE));
            String id = SpecialBlockItemBlock.getIdFromArmorStandName(armorStand.getCustomName());
            SpecialBlockItemBlock itemBlock 
                    = (SpecialBlockItemBlock) SpecialBlockInventoryData.getSpecialBlock(id);
            if(itemBlock==null) {
                return;
            }
            int dura;
            ItemMeta meta = item.getItemMeta();
            if(meta instanceof Damageable) {
                dura = itemBlock.getPreviousDurability(((Damageable)meta).getDamage());
                ((Damageable)meta).setDamage(dura);
                item.setItemMeta(meta);
                armorStand.setHelmet(item);
            }
        }
    }
    
    private BlockDataManager getOrCreateBlockDataManager(Player player) {
        BlockDataManager result = playerBlockDataManager.get(player);
        if(result==null) {
            result = new BlockDataManager();
            playerBlockDataManager.put(player, result);
        }
        return result;
    }
    
    private void sendNotEnabledErrorMessage(Player player) {
        PluginData.getMessageUtil().sendErrorMessage(player, "Block editor is not enabled for this world.");
    }
        

}
