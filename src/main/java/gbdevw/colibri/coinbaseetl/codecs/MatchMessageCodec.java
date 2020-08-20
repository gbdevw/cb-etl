package gbdevw.colibri.coinbaseetl.codecs;

import com.google.protobuf.InvalidProtocolBufferException;

import gbdevw.colibri.domain.Match;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Codec for the event bus for Match objects
 */
public class MatchMessageCodec implements MessageCodec<Match, Match> {

    @Override
    public void encodeToWire(Buffer buffer, Match s) {
        byte[] b = s.toByteArray();
        buffer.appendInt(b.length);
        buffer.appendBytes(b);
    }

    @Override
    public Match decodeFromWire(int pos, Buffer buffer) {

        try {
            int length = buffer.getInt(pos);
            return Match.parseFrom(buffer.getBytes(pos + Integer.BYTES, pos + Integer.BYTES + length));
        } 
        catch (InvalidProtocolBufferException e) 
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Match transform(Match s) {
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