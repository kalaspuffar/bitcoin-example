package org.ea.main;

import org.ea.messages.data.VarInt;

public class TestVarInt {
    public static void main(String[] arg) {
        VarInt vi = new VarInt(new byte[] { -0x04 });
        System.out.println(vi.getValue());

        VarInt vi2 = new VarInt(new byte[] { -0x03, -0x01, -0x01 });
        System.out.println(vi2.getValue());

        VarInt vi3 = new VarInt(new byte[] { -0x02, -0x01, -0x01, -0x01, -0x01 });
        System.out.println(vi3.getValue());

        VarInt vi4 = new VarInt(new byte[] { -0x01, -0x01, -0x01, -0x01, -0x01, -0x01, -0x01, -0x01, -0x01 });
        System.out.println(vi4.getValue());

    }
}
