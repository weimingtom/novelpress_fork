package novel.press;

import java.util.*;

////////////////////////////////////////////////////////////////////////////////////////////////////
//FUtil
////////////////////////////////////////////////////////////////////////////////////////////////////
public class FUtil {
	private static long _sleepAnchor;
	private static int _transJST;
	
	//コンストラクタ
	private FUtil() {}
	static {
		//標準時計算
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(0));
		_transJST = 9-cal.get(Calendar.HOUR_OF_DAY);
	}
	public static long _getTime(long time) {
		return time+_transJST*60*60*1000;
	}
	public static int _getColor(int r,int g,int b) {
		return (r<<16)|(g<<8)|(b);
	}
	public static int _getRevColor(int color) {
		return
			(255-(color>>16&0xff))<<16|
			(255-(color>> 8&0xff))<< 8|
			(255-(color    &0xff));
	}
	public static int _getMidColor(int color,int color2) {
		return 
			(((color>>16&0xff)+(color2>>16&0xff))/2)<<16 |
			(((color>> 8&0xff)+(color2>> 8&0xff))/2)<< 8 |
			(((color    &0xff)+(color2    &0xff))/2);
	}
	//byte bool
	public static boolean[] _byteToBool(byte[] bytes) {
		boolean[] bools = new boolean[8 * bytes.length];
		for(int i = 0; i < bytes.length; i++) {
			for(int j = 0; j < 8; j++) {
				if(( bytes[i] & 1 << ( 7 - j ) ) != 0) {
					bools[8 * i + j] = true;
				}else {
					bools[8 * i + j] = false;
				}
			}
		}
		return bools;
	}
	public static byte[] _boolToByte(boolean[] bools) {
		byte[] bytes = new byte[bools.length/8+(bools.length%8==0?0:1)];
		for(int i=0; i<bools.length; i++) {
			if(bools[i]) {
				bytes[i/8] = (byte)(bytes[i/8] | 1 << ( 7 - i%8 ));
			}
		}
		return bytes;
	}
	public static int _randomInt(int span) {
		long seed = System.currentTimeMillis();
		Random rand = new Random();
		rand.setSeed(seed);
		while(System.currentTimeMillis()==seed) ;
		return (rand.nextInt()>>>1)%span;
	}
	public static String _formatNum(int num,int len) {
		int o = 10;
		for(int i=0; i<len-1; i++) o *= 10;
		return String.valueOf(num+o).substring(1);
	}
	public static String _deleteStr(String str,String delete) {
		//不正
		if(str==null || str.length()==0) return null;
		if(delete==null || delete.length()==0) return str;
		//削除処理
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			if(delete.indexOf(c)<0) sb.append(c);
		}
		if(sb.length()==0) return null;
		else return sb.toString();
	}
	public static String[] _splitString(String str,char splitC) {
		//空白の場合はnullを入れる
		int index;
		Vector<String> vec = new Vector<String>();
		StringBuffer sb = new StringBuffer();
		
		if(str==null) return null;
		//解析
		index = 0;
		while(index<str.length()) {
			char c = str.charAt(index);
			if(c==splitC) {
				if(sb.length()==0) vec.addElement(null);
				else {
					vec.addElement(sb.toString());
					sb.delete(0,sb.length());
				}
			}else {
				sb.append(c);
			}
			index++;
		}
		//残り
		if(sb.length()==0) vec.addElement(null);
		else vec.addElement(sb.toString());
		//格納
		String[] result = new String[vec.size()];
		for(int i=0; i<vec.size(); i++) {
			if(vec.elementAt(i)!=null) result[i] = (String)vec.elementAt(i);
		}
		return result;
	}
	public static int _formatNum(int num,int min,int max) {
		if(num<min) num = min;
		if(num>max) num = max;
		return num;
	}
	public static int _parseNum(String str) {
		if(str==null || str.length()==0) return -1;
		return Integer.parseInt(str);
	}
	//sleep
	public static void _sleep(int time) {
		while(System.currentTimeMillis()<FUtil._sleepAnchor+time);
		FUtil._sleepAnchor = System.currentTimeMillis();
	}


}

