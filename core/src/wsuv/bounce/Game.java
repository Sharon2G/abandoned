package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.List;
import java.util.Random;

public class Game extends com.badlogic.gdx.Game {
    public static final String RSC_GAMEOVER_IMG = "gameover.png";
    public static final String RSC_MONO_FONT_FILE = "JetBrainsMono-Regular.ttf";
    public static final String RSC_MONO_FONT = "JBM.ttf";
    public static final String RSC_START_IMG = "StartImageV2.png";
    public static final String RSC_WALK_TEXTURE = "Walk/Chara_BlueWalk%05d.png";
    public static final String RSC_ENEMY_TEXTURE = "SlimeGreen/SlimeBasic_%05d.png";
    public static final String RSC_IDLE_TEXTURE = "Idle/Chara - BlueIdle%05d.png";
    public static final String RSC_JUMP_TEXTURE = "Jump/CharaWizardJump_%05d.png";
    public static final String RSC_SLASH_TEXTURE = "Slash/warrior_skill1_frame%d.png";
    public static final String RSC_BAT1_IMG = "bat1.png";
    public static final String RSC_BAT2_IMG = "bat2.png";
    public static final String RSC_BAT3_IMG = "bat3.png";
    public static final String RSC_BAT4_IMG = "bat4.png";


    AssetManager am;
    SpriteBatch batch;
    Random random = new Random();
    List<Node> path;


    Music music;
    @Override
    public void create() {
        am = new AssetManager();


        FileHandleResolver resolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFont.fontFileName = RSC_MONO_FONT_FILE;
        myFont.fontParameters.size = 210;
        am.load(RSC_MONO_FONT, BitmapFont.class, myFont);

        am.load(RSC_GAMEOVER_IMG, Texture.class);
        am.load(RSC_GAMEOVER_IMG, Texture.class);
        am.load(RSC_START_IMG, Texture.class);
        am.load(RSC_BAT1_IMG, Texture.class);
        am.load(RSC_BAT2_IMG, Texture.class);
        am.load(RSC_BAT3_IMG, Texture.class);
        am.load(RSC_BAT4_IMG, Texture.class);

        loadAnimationTextures();
        
        // Load Sounds
        batch = new SpriteBatch();
        setScreen(new LoadScreen(this, 1));

        // start the music right away.
        // this one we'll only reference via the GameInstance, and it's streamed
        // so, no need to add it to the AssetManager...
        music = Gdx.audio.newMusic(Gdx.files.internal("sadshark.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    private void loadAnimationTextures() {
        for (int i = 0; i <= 19; i++) {
            String fileName = String.format(RSC_WALK_TEXTURE, i);
            am.load(fileName, Texture.class);
        }

        for (int i = 0; i <= 29; i++) {
            String fileName = String.format(RSC_ENEMY_TEXTURE, i);
            am.load(fileName, Texture.class);
        }

        for (int i = 0; i <= 19; i++) {
            String fileName = String.format(RSC_IDLE_TEXTURE, i);
            am.load(fileName, Texture.class);
        }

        for (int i = 0; i <= 7; i++) {
            String fileName = String.format(RSC_JUMP_TEXTURE, i);
            am.load(fileName, Texture.class);
        }

        for (int i = 1; i <= 10; i++) {
            String fileName = String.format(RSC_SLASH_TEXTURE, i);
            am.load(fileName, Texture.class);
        }

    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
    }
}