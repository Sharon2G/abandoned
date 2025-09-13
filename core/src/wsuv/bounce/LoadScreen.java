package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadScreen extends ScreenAdapter {
    Game game;
    int level;
    Texture image;

    public LoadScreen(Game game, int level) {
        this.game = game;
        this.level = level;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        game.am.update(10);

        if (image == null && game.am.isLoaded(game.RSC_START_IMG)) {
            image = game.am.get(game.RSC_START_IMG);
        }

        if(image != null) {
            game.batch.begin();
            game.batch.draw(game.am.get(game.RSC_START_IMG, Texture.class), 0, -100, 800, 750);
            game.batch.end();
        }

        if (game.am.isFinished()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                if (game.am.isFinished())
                    game.setScreen(new Level1(game, 1331, true, level));
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                if (game.am.isFinished())
                    game.setScreen(new Level1(game, 1331,false, level));
            }
        }
//        else if (image != null) {
//            game.batch.begin();
//            game.batch.draw(game.am.get(game.RSC_START_IMG, Texture.class), 0, -100, 800, 750);
//            game.batch.end();
//        }
    }
}
