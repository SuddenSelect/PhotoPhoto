package pl.edu.pw.mmajews2.photophoto.manipulation;

/**
 * Created by Maciej Majewski on 2016-05-07.
 */
class BitInputStream {
    private byte[] content;
    private int iterator = 0;
    private StringBuilder buffer = new StringBuilder(16);

    public BitInputStream(byte[] content) {
        this.content = content;
    }

    public byte getMask(int amount){
        byte result = 0;
        byte bit = (byte)1;
        for (int i = 0; i < amount; i++) {
            result |= bit;
            bit *= (byte)2;
        }
        return (byte) (0xFF ^ result);
    }

    public int getBits(int amount){
        if(amount > buffer.length()){
            String bitString = "00000000"+Integer.toBinaryString(content[iterator]);
            bitString = bitString.substring(bitString.length()-8, bitString.length());
            buffer.append(bitString);
            iterator += 1;
        }

        int result = Integer.parseInt(buffer.substring(0, amount), 2);
        buffer.delete(0, amount);


        return result;
    }
}
