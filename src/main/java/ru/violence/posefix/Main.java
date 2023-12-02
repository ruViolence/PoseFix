package ru.violence.posefix;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(ListenerPriority.valueOf(getConfig().getString("priority"))));
    }

    public class PacketListener extends PacketAdapter {
        public PacketListener(ListenerPriority priority) {
            super(Main.this, priority, Collections.singletonList(PacketType.Play.Server.ENTITY_METADATA), ListenerOptions.ASYNC);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.isCancelled()) return;

            // Check if the entity is the player themselves
            if (event.getPlayer().getEntityId() == event.getPacket().getIntegers().read(0)) {
                PacketContainer container = event.getPacket().deepClone(); // Clone the packet

                StructureModifier<List<WrappedDataValue>> modifier = container.getDataValueCollectionModifier();

                List<WrappedDataValue> data = modifier.read(0);
                boolean modified = false;

                // Remove pose-related data
                for (Iterator<WrappedDataValue> iterator = data.iterator(); iterator.hasNext(); ) {
                    if (iterator.next().getIndex() == 6) {
                        iterator.remove();
                        modified = true;
                        break;
                    }
                }

                if (!modified) return;

                if (data.isEmpty()) {
                    event.setCancelled(true);
                } else {
                    modifier.write(0, data);
                    event.setPacket(container);
                }
            }
        }
    }
}
