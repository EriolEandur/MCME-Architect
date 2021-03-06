/*
 * Copyright (C) 2018 Eriol_Eandur
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
package com.mcmiddleearth.architect.serverResoucePack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Eriol_Eandur
 */
public class RPSwitchTask extends BukkitRunnable {

    
    @Override
    public void run() {
        for(Player player: Bukkit.getOnlinePlayers()) {
            RpPlayerData data = RpManager.getPlayerData(player);
            if(data.isAutoRp()) {
                RpRegion newRegion = RpManager.getRegion(player.getLocation());
                if(newRegion != data.getCurrentRegion()) {
                    data.setCurrentRegion(newRegion);
                    if(newRegion!=null) {
                        RpManager.setRp(newRegion.getRp(), player);
                    }
                }
            }
        }
    }
    
}
