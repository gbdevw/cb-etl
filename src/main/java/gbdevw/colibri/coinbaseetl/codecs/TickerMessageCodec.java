package gbdevw.colibri.coinbaseetl.codecs;

import com.google.protobuf.InvalidProtocolBufferException;

import gbdevw.colibri.domain.Ticker;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Codec for the event bus for Ticker objects
 */
public class TickerMessageCodec implements MessageCodec<Ticker, Ticker> {

    @Override
    public void encodeToWire(Buffer buffer, Ticker s) {
        byte[] b = s.toByteArray();
        buffer.appendInt(b.length);
        buffer.appendBytes(b);
    }

    @Override
    public Ticker decodeFromWire(int pos, Buffer buffer) {
        
        try {
            int length = buffer.getInt(pos);
            return Ticker.parseFrom(buffer.getBytes(pos + Integer.BYTES, pos + Integer.BYTES + length));
        } 
        catch (InvalidProtocolBufferException e) 
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Ticker transform(Ticker s) {
        return s;
    }

    @Override
    public String name() {
        return this.getClass().getName();
    }

    @Override
    public byte systemCodecID() {
        // Always -1
        return -1;
    }
}