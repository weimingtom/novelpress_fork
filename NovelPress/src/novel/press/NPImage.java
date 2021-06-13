package novel.press;

import android.graphics.Bitmap;

////////////////////////////////////////////////////////////////////////////////////////////////////
// NPImage
////////////////////////////////////////////////////////////////////////////////////////////////////
public class NPImage {
	
	private Bitmap image;
	private boolean back;
	private int
		imgNo,
		x,
		y;
	private boolean created;
	
	//コンストラクタ
	public NPImage(boolean back,int imgNo) {
		this.back  = back;
		this.imgNo = imgNo;
		created = false;
	}
	//clone
	public NPImage clone() {
		NPImage c = new NPImage(back,imgNo);
		c.x = x;
		c.y = y;
		return c;
	}
	//set
	public void setImgNo(int imgNo) {
		this.imgNo = imgNo;
		image = null;
		created = false;
	}
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	//get
	public int getImgNo() {
		return imgNo;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public Bitmap getImage() {
		//生成
		if(this.image==null && this.created==false) {
			if(this.imgNo>=0) {
				String name = ((this.back)?"imageB/":"imageF/")+FUtil._formatNum(this.imgNo,2);
				this.image = NovelPress._loadImage(name);
			}
			this.created = true;
		}
		return this.image;
	}
	//dispose
	public void dispose() {
		image = null;
		imgNo = -1;
		created = false;
	}
	//draw
	/*public void draw(Graphics g) {
		//生成
		if(image==null && created==false) {
			if(imgNo>=0) {
				image = NPCan.getImage(back,imgNo);
			}
			created = true;
		}
		//描画
		if(image!=null) {
			g.drawImage(image,x,y);
		}
	}*/
	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////

}
