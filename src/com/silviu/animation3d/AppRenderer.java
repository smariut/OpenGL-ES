package com.silviu.animation3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.egl.*;

import android.app.Activity;
import android.opengl.GLSurfaceView.*;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.SystemClock;
import android.widget.FrameLayout;

public class AppRenderer extends GLSurfaceView implements Renderer
{
	private static float mAngCtr = 0; 
	long mLastTime = SystemClock.elapsedRealtime();

	//unghiul si pozitia camerei
	static float camXang = 0.0001f;
	static float camYang = 180.0001f;

	static float camXpos = 0.0001f;
	static float camYpos = 60.0001f;
	static float camZpos = 180.0001f; 

	//distanta de la camera
	float mViewRad = 100;

	static float targetY = 0;
	static float targetX = 0;
	static float targetZ = 0;
	
	//dimensiuni ecran
	float scrHeight = 0; 	
	float scrWidth  = 0; 	
	float scrRatio  = 0; 
	float clipStart = 1; 
	
	final double deg2rad = Math.PI / 180.0; //grade->radian
	final double rad2deg = 180.0 / Math.PI; //radian ->grade 
	
	//constante pentru obiecte
	final int ball  = 2;
	final int triangle  = 5;

	
	//lungimi vertex buffer
	int[] bufferLen = new int[] {0,0,0,0,0,0,0}; 
	EGLDisplay display = null;
	EGLSurface bufferSurface = null;
	EGLSurface curSurface = null;
	boolean surfaceToggle = true;

	//parametrii mingii
	int ballRad = 20; //raza
	int ballVSliceCnt = 20; 
	int ballHSliceCnt = 20;  
	
	//parametrii triunghiului
	int streamCnt = 10; 
	int trianglesPerStream = 30; 

	float[][] dropCoords = new float[streamCnt*trianglesPerStream][3];

	int poolSliceCnt = streamCnt; 
	float poolRad = 57f; 
	public float AccelZ = 0;
	public float AccelY = 0;
	int orientation = 0; 
	
	//optiuni de afisare
	public boolean ShowBall = true;
	public boolean Paused = false;
	
	AppRenderer(Activity pActivity)
	{
		super(pActivity);
		FrameLayout layout = new FrameLayout(pActivity);
		layout.addView(this); 								//adaugarea suprafetei openGL 
		pActivity.setContentView(layout);
		setRenderer(this); 									//initializarea 
	}

	@Override
	public void onSurfaceCreated(GL10 gl1, EGLConfig pConfig) 
	{
		GL11 gl = (GL11)gl1; 
		
		//setarea culorii de fundal
		gl.glClearColor(1f, 0f, 0f, 1.0f); //rosu
		
		CreateBall(gl);
		CreateTriangle(gl);

	}

	void CreateBall(GL11 gl)
	{
		float x[][] = new float[ballVSliceCnt+1][ballHSliceCnt+1];
		float y[][] = new float[ballVSliceCnt+1][ballHSliceCnt+1];
		float z[][] = new float[ballVSliceCnt+1][ballHSliceCnt+1];

		for (int vCtr = 0; vCtr <= ballVSliceCnt; vCtr++) 
		{ 
			double vAng = 180.0 / ballVSliceCnt * vCtr; 
			float sliceRad = (float) (ballRad * Math.sin(vAng * deg2rad)); 
			float sliceY = (float) (ballRad * Math.cos(vAng * deg2rad)); 
			float vertexY = sliceY; 
			float vertexX = 0; 
			float vertexZ = 0; 
			
			for (int hCtr = 0; hCtr <= ballHSliceCnt; hCtr++) 
			{ 
				double hAng = 360.0 / ballHSliceCnt * hCtr; 
				vertexX = (float) (sliceRad * Math.sin(hAng * deg2rad)); 
				vertexZ = (float) (sliceRad * Math.cos(hAng * deg2rad));
				y[vCtr][hCtr]=vertexY;
				x[vCtr][hCtr]=vertexX;
				z[vCtr][hCtr]=vertexZ;
			} 
		} 
		int hCnt = x[0].length;
		int vCnt = x.length;;

		float vtx[] = new float[ballVSliceCnt*ballHSliceCnt/2*2*3*3];
		int vtxCtr = 0;
		for (int vCtr = 1; vCtr < vCnt; vCtr++)
			
			for (int hCtr = 1+vCtr%2; hCtr < hCnt; hCtr += 2)
			{
				vtx[vtxCtr]	= x[vCtr-1][hCtr-1];
				vtx[vtxCtr+ 1] = y[vCtr-1][hCtr-1];
				vtx[vtxCtr+ 2] = z[vCtr-1][hCtr-1];
				vtx[vtxCtr+ 3] = x[vCtr][hCtr-1];
				vtx[vtxCtr+ 4] = y[vCtr][hCtr-1];
				vtx[vtxCtr+ 5] = z[vCtr][hCtr-1];
				vtx[vtxCtr+ 6] = x[vCtr-1][hCtr];
				vtx[vtxCtr+ 7] = y[vCtr-1][hCtr];
				vtx[vtxCtr+ 8] = z[vCtr-1][hCtr];
				vtx[vtxCtr+ 9] = x[vCtr][hCtr-1];
				vtx[vtxCtr+10] = y[vCtr][hCtr-1];
				vtx[vtxCtr+11] = z[vCtr][hCtr-1];
				vtx[vtxCtr+12] = x[vCtr-1][hCtr];
				vtx[vtxCtr+13] = y[vCtr-1][hCtr];
				vtx[vtxCtr+14] = z[vCtr-1][hCtr];
				vtx[vtxCtr+15] = x[vCtr][hCtr];
				vtx[vtxCtr+16] = y[vCtr][hCtr];
				vtx[vtxCtr+17] = z[vCtr][hCtr];
				vtxCtr+=18;
			}
		
		StoreVertexData(gl, vtx, ball); 
	}

	void CreateTriangle(GL11 gl)
	{
		float vtx[] = {
				// X,  Y, Z
				  0f, 30f, 0,
				 -30f,-30f, 0,
				  30f,-30f, 0
				};
		
		StoreVertexData(gl, vtx, triangle); 
	}

	void StoreVertexData(GL11 gl, float[] pVertices, int pObjectNum)
	{
		FloatBuffer buffer = ByteBuffer.allocateDirect(pVertices.length * 4) 	//float este 4 byti
		 .order(ByteOrder.nativeOrder())
		 .asFloatBuffer()  
		 .put(pVertices);	

		(gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, pObjectNum); 
		buffer.position(0);
		
		(gl).glBufferData(GL11.GL_ARRAY_BUFFER, buffer.capacity()*4, buffer, GL11.GL_STATIC_DRAW);
		(gl).glBindBuffer(GL11.GL_ARRAY_BUFFER, 0); 
		bufferLen[pObjectNum] = buffer.capacity()/3; 
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int pWidth, int pHeight) 
	{
		gl.glViewport(0, 0, pWidth, pHeight); 							//vederea de pe ecran
		
		scrHeight = pHeight;
		scrWidth = pWidth;
		scrRatio = scrWidth/scrHeight;
		
		
		gl.glMatrixMode(GL11.GL_PROJECTION);		
		gl.glLoadIdentity();						
		
		float camDist = (float)Math.sqrt(camXpos*camXpos+camYpos*camYpos+camZpos*camZpos);
		clipStart = Math.max(2, camDist-185); //max scene radius is 185 points at corners
		
		gl.glFrustumf(  
				-scrRatio*.5f*clipStart,
				scrRatio*.5f*clipStart, 
				-1f*.5f*clipStart, 
				1f*.5f*clipStart, 
				clipStart, 
				clipStart+185+Math.min(185, camDist));
		
		gl.glEnable(GL11.GL_DEPTH_TEST);
		
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		orientation = getResources().getConfiguration().orientation;
	}
	@Override
	public void onDrawFrame(GL10 gl1) 
	{
		GL11 gl = (GL11)gl1; 
		
		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();   

		GLU.gluLookAt(gl, camXpos, camYpos, camZpos, targetX, targetY, targetZ, 0f, 100.0f, 0.0f);
		
		long now = SystemClock.elapsedRealtime();
		long diff = now - mLastTime;
		mLastTime = now;

		if (!Paused) 
		{
			mAngCtr += diff/100.0;
			if (mAngCtr > 360) mAngCtr -= 360; 
		}
		
		//desenarea scenei
		DrawSceneObjects(gl);

	}
	
	void DrawSceneObjects(GL11 gl)
	{
		if (ShowBall) 
		{
			//desenarea primei culori
			gl.glPushMatrix();
			gl.glColor4f(1f, 1f, 0f, 1f); //galben
			gl.glRotatef(mAngCtr, 0.0f, 1.0f, 0f); 
			DrawObject(gl, GL11.GL_TRIANGLES, ball);
			gl.glPopMatrix();
			
			//desenarea celei de a doua culori
			gl.glPushMatrix();
			gl.glColor4f(0f, 1f, 0f, 1f); //verde
			gl.glRotatef(mAngCtr+360f/ballHSliceCnt, 0.0f, 1.0f, 0f);
			DrawObject(gl, GL11.GL_TRIANGLES, ball);
			gl.glPopMatrix();
		}
			DrawFountain(gl);
	}
//////////////////////////////////////////////////////////////////////////////////	
	void DrawObject(GL11 gl, int pShapeType, int pObjNum)
	{
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, pObjNum);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		//desenarea triunghiurilor
		gl.glDrawArrays(pShapeType, 0, bufferLen[pObjNum]);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
	}

	void DrawFountain(GL11 gl)
	{
		float angY = 270-(float)Math.atan2(camZpos,camXpos)*(float)rad2deg; 
		float hypLen = (float)Math.sqrt(camXpos*camXpos+camZpos*camZpos); 
		float angX = (float)Math.atan2(camYpos,hypLen)*(float)rad2deg; 

		gl.glColor4f(0f, 0f, 1f, 0.5f); //albastru
		DrawDropTriangles(gl, angX, angY, dropCoords); 
	}

	void DrawDropTriangles(GL11 gl, float pAngX, float pAngY, float[][] pDropCoords)
	{		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, triangle);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		
			gl.glDrawArrays(GL11.GL_TRIANGLES, 0, bufferLen[triangle]); 
			gl.glPopMatrix(); 
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0); 
	}

	void ShowMaxDepthBits() 
	{
		EGL10 egl = (EGL10)EGLContext.getEGL();
		EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		EGLConfig[] conf = new EGLConfig[100]; 
		egl.eglGetConfigs(dpy, conf, 100, null);
		int maxBits = 0;
		int[] value = new int[1]; 
		for(int i = 0; i < 100 && conf[i] != null; i++)
		{
			egl.eglGetConfigAttrib(dpy, conf[i], EGL10.EGL_DEPTH_SIZE, value);
			maxBits = value[0]>maxBits ? value[0] : maxBits;
		}
	}
}