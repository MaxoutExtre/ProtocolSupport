package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_13;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleChunk;
import protocolsupport.protocol.packet.middleimpl.ClientBoundPacketData;
import protocolsupport.protocol.serializer.ArraySerializer;
import protocolsupport.protocol.serializer.ItemStackSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.basic.TileNBTRemapper;
import protocolsupport.protocol.typeremapper.block.FlatteningBlockId;
import protocolsupport.protocol.typeremapper.block.LegacyBlockData;
import protocolsupport.protocol.typeremapper.chunk.ChunkTransformerBB;
import protocolsupport.protocol.typeremapper.chunk.ChunkTransformerVaries;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableSingletonList;

public class Chunk extends MiddleChunk {

	public Chunk(ConnectionImpl connection) {
		super(connection);
	}

	protected final ChunkTransformerBB transformer = new ChunkTransformerVaries(
		LegacyBlockData.REGISTRY.getTable(connection.getVersion()),
		FlatteningBlockId.REGISTRY.getTable(connection.getVersion())
	);

	@Override
	public RecyclableCollection<ClientBoundPacketData> toData() {
		transformer.loadData(data, bitmask, cache.getAttributesCache().hasSkyLightInCurrentDimension(), full);
		ProtocolVersion version = connection.getVersion();
		ClientBoundPacketData serializer = ClientBoundPacketData.create(ClientBoundPacket.PLAY_CHUNK_SINGLE_ID);
		serializer.writeInt(chunkX);
		serializer.writeInt(chunkZ);
		serializer.writeBoolean(full);
		VarNumberSerializer.writeVarInt(serializer, bitmask);
		ArraySerializer.writeVarIntByteArray(serializer, transformer::writeLegacyData);
		ArraySerializer.writeVarIntTArray(serializer, tiles, (to, tile) -> ItemStackSerializer.writeTag(to, version, TileNBTRemapper.remap(version, tile)));
		return RecyclableSingletonList.create(serializer);
	}

}
