package newRaspiServer;

import java.util.BitSet;

public class Utils {
	 private static final long MASK_16_BITS = 0xFFFFL;
     private static final int MASK_BIT_1 = 0x1;
     public static final int MAXLENGTH = 65535;
	public static void main(String[] args) {
		int define=65535;
		BitSet bs = BitSet.valueOf(new long[]{define});
		bs.set(0, false);
		bs.set(1, false);
		bs.set(2, false);
		bs.set(3, false);
		bs.set(4, false);
		bs.set(5, false);
		bs.set(6, false);
		bs.set(7, false);
		bs.set(8, false);
		bs.set(9, false);
		bs.set(10, false);
		bs.set(11, false);
		bs.set(12, false);
		bs.set(13, false);
		bs.set(14, false);
		bs.set(15, false);
		
		
		
		System.out.println(bitSetToInt(bs));
	}
	


	  public static int setBit(int bit, int target) {
	        // Create mask
	        int mask = 1 << bit;
	        // Set bit
	        return target | mask;
	     }

	  public static int bitSetToInt(BitSet bitSet)
	  {
	      int bitInteger = 0;
	      for(int i = 0 ; i < 32; i++)
	          if(bitSet.get(i))
	              bitInteger |= (1 << i);
	      return bitInteger;
	  }

	/**
	 * @return the maskBit1
	 */
	public static int getMaskBit1() {
		return MASK_BIT_1;
	}

	/**
	 * @return the mask16Bits
	 */
	public static long getMask16Bits() {
		return MASK_16_BITS;
	}
}

