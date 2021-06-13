package novel.press;

import java.util.Vector;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class NPCanvasView extends SurfaceView implements SurfaceHolder.Callback,Runnable {
	protected Thread thread;
	protected int[] tchUp;
	protected Vector<int[]> tchNow = new Vector<int[]>();
	protected Canvas can;
	protected Paint p;
	protected int fontSize;
	protected int fontSizeBuf;

	public NPCanvasView(Context context) {
		super(context);
        SurfaceHolder holder = getHolder(); 
        holder.addCallback(this);
        holder.setFixedSize(getWidth(),getHeight());
		//Paintを生成する
		this.p = new Paint();
		this.p.setAntiAlias(true);
		this.p.setTypeface(Typeface.MONOSPACE);
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//スレッドを開始する
        if(this.thread==null) {
        	this.thread = new Thread(this);
        	this.thread.start();
        }
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//アクションを判定する
		int action = event.getAction()&MotionEvent.ACTION_MASK;
		//
		if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_POINTER_UP) {
			int pIndex = (event.getAction()&MotionEvent.ACTION_POINTER_ID_MASK)>>MotionEvent.ACTION_POINTER_ID_SHIFT;
			int pID = event.getPointerId(pIndex);
			for(int i=0; i<this.tchNow.size(); i++) {
				if(this.tchNow.elementAt(i)[0]==pID) {
					this.tchNow.removeElementAt(i);
					break;
				}
			}
			this.tchUp = new int[]{
				(int)event.getX(pID),
				(int)event.getY(pID)};
		}
		if(event.getAction()==MotionEvent.ACTION_MOVE) {
			int nooP = event.getPointerCount();
			for(int i=0; i<nooP; i++) {
				int pID = event.getPointerId(i);
				for(int[] point:this.tchNow) {
					if(point[0]==pID) {
						point[1] = (int)event.getX(pID);
						point[2] = (int)event.getY(pID);
						break;
					}
				}
			}
		}
		if(event.getAction()==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN) {
			int pIndex = (event.getAction()&MotionEvent.ACTION_POINTER_ID_MASK)>>MotionEvent.ACTION_POINTER_ID_SHIFT;
			int pID = event.getPointerId(pIndex);
			this.tchNow.addElement(new int[]{pID,
					(int)event.getX(pID),
					(int)event.getY(pID)});
		}
		return true;
	}
	protected void fillRect(int x,int y,int w,int h,int color) {
		int l = drawX+x*drawRate/_RATE;
		int t = drawY+y*drawRate/_RATE;
		int r = l+w*drawRate/_RATE;
		int b = t+h*drawRate/_RATE;
		this.p.setStyle(Style.FILL);
		this.p.setColor(color);
		this.can.drawRect(l,t,r,b,this.p);
	}
	protected void drawRect(int x,int y,int w,int h,int color) {
		int l = drawX+x*drawRate/_RATE;
		int t = drawY+y*drawRate/_RATE;
		int r = l+w*drawRate/_RATE;
		int b = t+h*drawRate/_RATE;
		this.p.setStyle(Style.STROKE);
		this.p.setColor(color);
		this.can.drawRect(l,t,r,b,this.p);
	}
	protected void drawString(String str,int x,int y,int color) {
		x = drawX+x*drawRate/_RATE;
		y = drawY+y*drawRate/_RATE;
		y += this.p.getTextSize();
		this.p.setColor(color);
		for(int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			this.can.drawText(""+c,x,y,this.p);
			x += stringWidth(c)*drawRate/_RATE;
		}
	}
	protected void drawImage(Bitmap image,int x,int y) {
		x = drawX+x*drawRate/_RATE;
		y = drawY+y*drawRate/_RATE;
		int w = image.getWidth()*drawRate/_RATE;
		int h = image.getHeight()*drawRate/_RATE;
		this.p.setAlpha(255);
		this.can.drawBitmap(image,new Rect(0,0,image.getWidth(),image.getHeight()),new Rect(x,y,x+w,y+h),this.p);
	}
	protected void drawImage(Bitmap image,int x,int y,int sx,int sy,int sw,int sh) {
		this.p.setAlpha(255);
		this.can.drawBitmap(image,new Rect(sx,sy,sx+sw,sy+sh),new Rect(x,y,x+sw,y+sh),this.p);
	}
	protected void fillRect(int color) {
		this.p.setColor(color);
		this.can.drawRect(drawX,drawY,drawX+drawW,drawY+drawH,this.p);
	}
	protected int[] transTouchXY(int x,int y) {
		return new int[]{
			(x-drawX)*_RATE/drawRate,
			(y-drawY)*_RATE/drawRate};
	}
	protected void startActivity(Class<? extends Activity> cls) {
		Activity parent = (Activity)getContext();
		Intent intent = new Intent(parent,cls);
		parent.startActivity(intent);
	}
	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////
	protected static final int _RATE = 1000;
	protected int
		wW,
		wH,
		drawX,
		drawY,
		drawW,
		drawH,
		drawW_raw,
		drawH_raw,
		drawRate;
	protected void setWindowSize(int wW,int wH) {
		this.wW = wW;
		this.wH = wH;
		if(drawW_raw>0 && drawH_raw>0) updateDrawRate();
	}
	protected void setDrawSize(int drawW,int drawH) {
		drawW_raw = drawW;
		drawH_raw = drawH;
		if(wW>0 && wH>0) updateDrawRate();
	}
	private void updateDrawRate() {
		//描画レートを計算する
		//(実寸値の何%で描画するのかという値)
		//(実際のpx値にレートを掛けて1000で割った大きさで描画する。1000:実寸大で描画するという意味になる)
		drawRate = Math.min(_RATE*wW/drawW_raw,_RATE*wH/drawH_raw);
		//画像を描画する矩形領域(Rect)を計算する
		drawW = drawW_raw*drawRate/_RATE;
		drawH = drawH_raw*drawRate/_RATE;
		drawX = (wW-drawW)/2;
		drawY = (wH-drawH)/2;
		//フォントサイズを変更する
		this.p.setTextSize(this.fontSize*drawRate/_RATE);
	}
	protected int _stringWidth(String str) {
		int w = 0;
		for(int i=0; i<str.length(); i++) w += stringWidth(str.charAt(i));
		return w;
	}
	protected int stringWidth(char c) {
		//半角文字
		if(
			c<=0x80                      ||//ASCII文字
			(c>='\uff61' && c<='\uff9f') ||//半角カタカナ
			c=='\u00a5'                  ||//半角￥（バックスラッシュ）
			c=='\u203e'                    //半角チルダ
			) return this.fontSize/2;
		//全角文字
		else return this.fontSize;
	}
	protected void setFontSize(int size) {
		//fontSize決定
		this.fontSize = size;
		if(this.fontSizeBuf==this.fontSize) return;
		this.fontSizeBuf = this.fontSize;
		//Paintに適用する
		this.p.setTextSize(this.fontSize*drawRate/_RATE);
	}
	
}

