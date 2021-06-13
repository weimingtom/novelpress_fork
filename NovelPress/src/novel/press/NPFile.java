package novel.press;

import java.io.*;

////////////////////////////////////////////////////////////////////////////////////////////////////
// NPFile
////////////////////////////////////////////////////////////////////////////////////////////////////
public class NPFile {
	//定数
	public static final int
		EFFECT_NONE  = 0,
		EFFECT_MONO  = 1,
		EFFECT_SEPIA = 2,
		EFFECT_CREV  = 3;
	public static final int
		SP_SUM  = (128/8)+103;//119

	public boolean[] var;
	public int
		index,
		fontSpeed,
		fontSize,
		fontColor,
		fontSpaceW,
		fontSpaceH,
		txtareaX,
		txtareaY,
		txtareaW,
		txtareaH,
		frameX,
		frameY,
		frameW,
		frameH,
		frameColor,
		frameA,
		effect;
	public boolean
		vib,
		savable;
	private boolean needRedraw;
	
	//img
	protected int biColor;
	protected NPImage bi;
	protected NPImage[] fi;
	//mld
	protected int[] mld;

	//コンストラクタ
	public NPFile() {
		
	}
	/*//clone
	public NPFile cloneBuf() {
		NPFile c = new NPFile();
		c.effect     = effect;
		if(bi==null) {
			c.biColor = biColor;
		}else {
			c.bi = bi.clone();
		}
		for(int i=0; i<fi.length; i++) {
			c.fi[i] = fi[i].clone();
		}
		return c;
	}
	public NPFile cloneRec() {
		NPFile c = new NPFile();
		c.index      = index;
		c.txtareaX   = txtareaX;
		c.txtareaY   = txtareaY;
		c.txtareaW   = txtareaW;
		c.txtareaH   = txtareaH;
		c.fontSpeed  = fontSpeed;
		c.fontSize   = fontSize;
		c.fontColor  = fontColor;
		c.fontSpaceW = fontSpaceW;
		c.fontSpaceH = fontSpaceH;
		c.frameX     = frameX;
		c.frameY     = frameY;
		c.frameW     = frameW;
		c.frameH     = frameH;
		c.frameColor = frameColor;
		c.frameA     = frameA;
		c.vib        = vib;
		c.effect     = effect;
		c.savable    = savable;
		if(bi==null) {
			c.biColor = biColor;
		}else {
			c.bi = bi.clone();
		}
		for(int i=0; i<fi.length; i++) {
			c.fi[i] = fi[i].clone();
		}
		c.mld = SoundPlayer._getList(NPCan.LIMIT_NOOMLD);
		System.arraycopy(var,0,c.var,0,var.length);
		return c;
	}*/

	protected int getFrameX() {
		return this.frameX;
	}
	protected int getFrameY() {
		return this.frameY;
	}
	protected int getFrameW() {
		return this.frameW;
	}
	protected int getFrameH() {
		return this.frameH;
	}
	protected int getFrameA() {
		return this.frameA;
	}
	protected int getFrameColor() {
		return this.frameColor;
	}
	protected NPImage getImageB() {
		return this.bi;
	}
	protected NPImage[] getImageF() {
		return this.fi;
	}
	protected int getImageBColor() {
		return this.biColor;
	}
	protected boolean isNeedRedraw() {
		return this.needRedraw;
	}
	protected void finRedraw() {
		this.needRedraw = false;
	}
	//bi変更
	public void setImageB(boolean isImg,int opt) {
		//画像
		if(isImg) {
			if(bi==null) {
				bi = new NPImage(true,opt);
			}else {
				bi.setImgNo(opt);
			}
		}
		//単色
		else {
			biColor = opt;
			bi = null;
		}
		this.needRedraw = true;
	}
	//fi変更
	public void setImageF(int fiNo,int imgNo) {
		if(fiNo>=fi.length) return;
		if(imgNo<0) {
			//消去
			if(fiNo<0) {
				for(int i=0; i<fi.length; i++) {
					fi[i].dispose();
				}
			}else {
				fi[fiNo].dispose();
			}
		}else {
			//変更
			if(fiNo<0) return;
			fi[fiNo].setImgNo(imgNo);
		}
		this.needRedraw = true;
	}
	public void setImageFXY(int fiNo,int x,int y) {
		if(fiNo<0 || fiNo>=fi.length) return;
		fi[fiNo].setX(x);
		fi[fiNo].setY(y);
		this.needRedraw = true;
	}
	public void setEffect(int effect) {
		this.effect = effect;
		this.needRedraw = true;
	}
	//save
	public void save(DataOutputStream dataOut) throws Exception {
		//time
		dataOut.writeLong(System.currentTimeMillis());
		//変数
		byte[] bs = FUtil._boolToByte(var);
		dataOut.write(bs);
		//データ
		dataOut.writeInt(index);
		dataOut.writeShort(txtareaX);
		dataOut.writeShort(txtareaY);
		dataOut.writeShort(txtareaW);
		dataOut.writeShort(txtareaH);
		dataOut.write(fontSpeed);
		dataOut.write(fontSize);
		dataOut.writeInt(fontColor);
		dataOut.write(fontSpaceW);
		dataOut.write(fontSpaceH);
		dataOut.writeShort(frameX);
		dataOut.writeShort(frameY);
		dataOut.writeShort(frameW);
		dataOut.writeShort(frameH);
		dataOut.writeInt(frameColor);
		dataOut.write(frameA);
		dataOut.writeBoolean(vib);
		dataOut.write(effect);
		//image
		if(bi==null) {
			dataOut.writeBoolean(false);
			dataOut.writeInt(biColor);
		}else {
			dataOut.writeBoolean(true);
			dataOut.write(bi.getImgNo());
		}
		for(int i=0; i<fi.length; i++) {
			dataOut.write(fi[i].getImgNo());
			dataOut.writeShort(fi[i].getX());
			dataOut.writeShort(fi[i].getY());
		}
		//mld
		for(int i=0; i<mld.length; i++) {
			dataOut.write(mld[i]);
		}
	}
	//load
	public void load(DataInputStream dataIn) throws Exception {
		//変数
		byte[] bs = new byte[128/8];
		dataIn.read(bs);
		var = FUtil._byteToBool(bs);
		//データ
		index      = dataIn.readInt();
		txtareaX   = dataIn.readShort();
		txtareaY   = dataIn.readShort();
		txtareaW   = dataIn.readShort();
		txtareaH   = dataIn.readShort();
		fontSpeed  = dataIn.readByte();
		fontSize   = dataIn.read();
		fontColor  = dataIn.readInt();
		fontSpaceW = dataIn.readByte();
		fontSpaceH = dataIn.readByte();
		frameX     = dataIn.readShort();
		frameY     = dataIn.readShort();
		frameW     = dataIn.readShort();
		frameH     = dataIn.readShort();
		frameColor = dataIn.readInt();
		frameA     = dataIn.readByte();
		vib        = dataIn.readBoolean();
		effect     = dataIn.read();
		//image
		if(dataIn.readBoolean()) {
			bi = new NPImage(true,dataIn.readByte());
		}else {
			biColor = dataIn.readInt();
		}
		for(int i=0; i<fi.length; i++) {
			fi[i].setImgNo(dataIn.readByte());
			fi[i].setX(dataIn.readShort());
			fi[i].setY(dataIn.readShort());
		}
		//mld
		mld = new int[NPCan.LIMIT_NOOMLD];
		for(int i=0; i<mld.length; i++) {
			mld[i] = dataIn.readByte();
			if(mld[i]>=0) SoundPlayer._play(mld[i],true,0);
		}
	}
	protected void init() {
		this.index = 0;
		fontSpeed = -1;
		fontSize  = 12;
		fontColor = 0xffffff;
		this.fontSpaceW = 0;
		this.fontSpaceH = 0;
		this.txtareaX = 0;
		this.txtareaY = 0;
		txtareaW  = NPCan.WIDTH;
		txtareaH  = NPCan.HEIGHT;
		this.frameX = 0;
		this.frameY = 0;
		frameW    = NPCan.WIDTH;
		frameH    = NPCan.HEIGHT;
		frameA    = -1;
		this.frameColor = 0x000000;
		this.effect = EFFECT_NONE;
		this.vib = false;
		savable   = true;
		this.needRedraw = true;
		this.biColor = 0x000000;
		fi = new NPImage[NPCan.LIMIT_NOOFI];
		for(int i=0; i<fi.length; i++) {
			fi[i] = new NPImage(false,-1);
		}
		var = new boolean[128];
	}
	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////
	private static NPFile
		_fileNow = new NPFile(),
		_fileRec = new NPFile(),
		_fileBuf;
	
	protected static NPFile _getFileNow() {
		return _fileNow;
	}
	protected static NPFile _getFileRec() {
		return _fileRec;
	}
	protected static NPFile _getFileBuf() {
		return _fileBuf;
	}
	protected static void _createFileBuf() {
		_fileBuf = new NPFile();
		_fileBuf.init();
	}
	protected static void _deleteFileBuf() {
		_fileBuf = null;
	}
}

