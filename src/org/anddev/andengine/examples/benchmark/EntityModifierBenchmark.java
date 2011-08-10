package org.anddev.andengine.examples.benchmark;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.DelayModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.sprite.batch.SpriteBatch;
import org.anddev.andengine.entity.sprite.batch.SpriteGroup;
import org.anddev.andengine.opengl.shader.ShaderProgram;
import org.anddev.andengine.opengl.shader.util.constants.ShaderProgramConstants;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.ITextureRegion;
import org.anddev.andengine.opengl.util.GLHelper;

import android.opengl.GLES20;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 20:24:17 - 27.06.2010
 */
public class EntityModifierBenchmark extends BaseBenchmark {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static final int SPRITE_COUNT = 1000;

	public static final String SHADERPROGRAM_VERTEXSHADER_DEFAULT =
			"uniform mat4 " + ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX + ";\n" +
			"attribute vec4 " + ShaderProgramConstants.ATTRIBUTE_POSITION + ";\n" +
			"attribute vec2 " + ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES + ";\n" +
			"varying vec2 " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n" +
			"void main() {\n" +
			"   " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + " = " + ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES + ";\n" +
			"   gl_Position = " + ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX + " * " + ShaderProgramConstants.ATTRIBUTE_POSITION + ";\n" +
			"}";

	public static final String SHADERPROGRAM_FRAGMENTSHADER_DEFAULT =
			"precision mediump float;\n" + // TODO Try 'precision lowp float;\n'
			"uniform sampler2D " + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ";\n" +
			"uniform float " + ShaderProgramConstants.UNIFORM_ALPHA + ";\n" +
			"varying vec2 " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n" +
			"void main() {\n" +
			"  gl_FragColor = vec4(1.0, 1.0, 1.0, " + ShaderProgramConstants.UNIFORM_ALPHA + ") * texture2D(" + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ", " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ");\n" +
			"}";
	
	// ===========================================================
	// Fields
	// ===========================================================

	private Camera mCamera;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mFaceTextureRegion;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected int getBenchmarkID() {
		return ENTITYMODIFIERBENCHMARK_ID;
	}

	@Override
	protected float getBenchmarkStartOffset() {
		return 2;
	}

	@Override
	protected float getBenchmarkDuration() {
		return 10;
	}

	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
	}

	@Override
	public void onLoadResources() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "gfx/face_box.png", 0, 0);

		this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
	}

	@Override
	public Scene onLoadScene() {
		final Scene scene = new Scene();
		scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

//		this.drawUsingSprites(scene);
//		this.drawUsingSpritesWithSharedVertexBuffer(scene);
		this.drawUsingSpriteBatch(scene);

		return scene;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void drawUsingSprites(final Scene pScene) {
		final IEntityModifier faceEntityModifier = new SequenceEntityModifier(
				new RotationByModifier(2, 90),
				new AlphaModifier(1.5f, 1, 0),
				new AlphaModifier(1.5f, 0, 1),
				new ScaleModifier(2.5f, 1, 0.5f),
				new DelayModifier(0.5f),
				new ParallelEntityModifier(
						new ScaleModifier(2f, 0.5f, 5),
						new RotationByModifier(2, 90)
				),
				new ParallelEntityModifier(
						new ScaleModifier(2f, 5, 1),
						new RotationModifier(2f, 180, 0)
				)
		);

		ShaderProgram shaderProgram = null;
		for(int i = 0; i < SPRITE_COUNT; i++) {
			final Sprite face = new Sprite((CAMERA_WIDTH - 32) * this.mRandom.nextFloat(), (CAMERA_HEIGHT - 32) * this.mRandom.nextFloat(), this.mFaceTextureRegion);
			if(shaderProgram == null) {
				face.setDefaultShaderProgram();
				shaderProgram = face.getShaderProgram();
			} else {
				face.setShaderProgram(shaderProgram);
			}
			face.registerEntityModifier(faceEntityModifier.clone());

			pScene.attachChild(face);
		}
	}

//	private void drawUsingSpritesWithSharedVertexBuffer(final Scene pScene) {
//		final IEntityModifier faceEntityModifier = new SequenceEntityModifier(
//				new RotationByModifier(2, 90),
//				new AlphaModifier(1.5f, 1, 0),
//				new AlphaModifier(1.5f, 0, 1),
//				new ScaleModifier(2.5f, 1, 0.5f),
//				new DelayModifier(0.5f),
//				new ParallelEntityModifier(
//						new ScaleModifier(2f, 0.5f, 5),
//						new RotationByModifier(2, 90)
//				),
//				new ParallelEntityModifier(
//						new ScaleModifier(2f, 5, 1),
//						new RotationModifier(2f, 180, 0)
//				)
//		);
//
//		/* As we are creating quite a lot of the same Sprites, we can let them share a VertexBuffer to significantly increase performance. */
//		final RectangleVertexBuffer sharedVertexBuffer = new RectangleVertexBuffer(GL11.GL_DYNAMIC_DRAW, true);
//		sharedVertexBuffer.update(this.mFaceTextureRegion.getWidth(), this.mFaceTextureRegion.getHeight());
//
//		for(int i = 0; i < SPRITE_COUNT; i++) {
//			final Sprite face = new Sprite((CAMERA_WIDTH - 32) * this.mRandom.nextFloat(), (CAMERA_HEIGHT - 32) * this.mRandom.nextFloat(), this.mFaceTextureRegion, sharedVertexBuffer);
//			face.registerEntityModifier(faceEntityModifier.clone());
//
//			pScene.attachChild(face);
//		}
//	}

	private void drawUsingSpriteBatch(final Scene pScene) {
		final IEntityModifier faceEntityModifier = new SequenceEntityModifier(
				new RotationByModifier(2, 90),
				//				new AlphaModifier(1.5f, 1, 0),
				//				new AlphaModifier(1.5f, 0, 1),
				new DelayModifier(1.5f + 1.5f),
				new ScaleModifier(2.5f, 1, 0.5f),
				new DelayModifier(0.5f),
				new ParallelEntityModifier(
						new ScaleModifier(2f, 0.5f, 5),
						new RotationByModifier(2, 90)
				),
				new ParallelEntityModifier(
						new ScaleModifier(2f, 5, 1),
						new RotationModifier(2f, 180, 0)
				)
		);

		final IEntityModifier spriteBatchEntityModifier = new SequenceEntityModifier(
				new DelayModifier(2),
				new AlphaModifier(1.5f, 1, 0),
				new AlphaModifier(1.5f, 0, 1)
		);

		final SpriteGroup spriteGroup = new SpriteGroup(this.mBitmapTextureAtlas, EntityModifierBenchmark.SPRITE_COUNT);

		final ShaderProgram shaderProgram = new ShaderProgram(EntityModifierBenchmark.SHADERPROGRAM_VERTEXSHADER_DEFAULT, EntityModifierBenchmark.SHADERPROGRAM_FRAGMENTSHADER_DEFAULT) {
			@Override
			public void bind() {
				super.bind();

				this.setUniform(ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX, GLHelper.getModelViewProjectionMatrix());
				this.setTexture(ShaderProgramConstants.UNIFORM_TEXTURE_0, 0);
				this.setUniform(ShaderProgramConstants.UNIFORM_ALPHA, spriteGroup.getAlpha());
			}
		};
		spriteGroup.setShaderProgram(shaderProgram);
//		spriteGroup.setDefaultShaderProgram();
		spriteGroup.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		for(int i = 0; i < EntityModifierBenchmark.SPRITE_COUNT; i++) {
			final Sprite face = new Sprite((EntityModifierBenchmark.CAMERA_WIDTH - 32) * this.mRandom.nextFloat(), (EntityModifierBenchmark.CAMERA_HEIGHT - 32) * this.mRandom.nextFloat(), this.mFaceTextureRegion);
			face.registerEntityModifier(faceEntityModifier.clone());

			spriteGroup.attachChild(face);
		}
		spriteGroup.registerEntityModifier(spriteBatchEntityModifier);

		pScene.attachChild(spriteGroup);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
