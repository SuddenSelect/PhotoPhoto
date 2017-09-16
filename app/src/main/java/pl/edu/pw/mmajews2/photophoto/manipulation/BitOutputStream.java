package pl.edu.pw.mmajews2.photophoto.manipulation;

/**
 * Created by Maciej Majewski on 2016-05-07.
 */
class BitOutputStream {
    private byte[] content;
    private int iterator = 0;
    private StringBuilder buffer = new StringBuilder(16);

    public BitOutputStream(int amount) {
        this.content = new byte[amount];
    }

    public byte getMask(int amount){
        byte result = 0;
        byte bit = (byte)1;
        for (int i = 0; i < amount; i++) {
            result |= bit;
            bit *= (byte)2;
        }
        return result;
    }


    public void putBits(int amount, int bits){
        String bitString = "00000000"+Integer.toBinaryString(bits);
        bitString = bitString.substring(bitString.length()-amount, bitString.length());
        buffer.append(bitString);
        if(buffer.length() >= 8){
            content[iterator] = (byte) Integer.parseInt(buffer.substring(0, 8), 2);
            buffer.delete(0, 8);
            iterator += 1;
        }
    }

    public byte[] getContent() {
        return content;
    }

}
