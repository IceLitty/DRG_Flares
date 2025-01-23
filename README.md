# DRG Flares (NeoForge 1.21.1)

# Introduce
This project is a mirror of original mod, target is upgrade to 1.21.1 with neoforge.
Yarn mapping is issued for me, so I changed it to parchment, its have many difference with yarn mappings,
so I don't work to fabric and forge mod loader and take PR to original project.

State: **Work In Progress**

# TODO
- `Entity.getGravity()` is final, try this
  - `me.lizardofoz.drgflares.entity.FlareEntity.applyGravity`
- To get vanilla packet, try invoke private method
  - `me.lizardofoz.drgflares.neoforge.packet.PacketStuff.sendFlareSpawnS2CPacket`
- [ ] Client Test
