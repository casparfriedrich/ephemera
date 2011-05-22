package ephemera;

import com.jme.app.SimpleGame;
import com.jme.bounding.CollisionTree;
import com.jme.bounding.CollisionTreeManager;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;

import ephemera.controller.SchwarmController;
import ephemera.model.World;




public class Main extends SimpleGame {
	
	World wc;
	SchwarmController 		schwarm;
	//CollisionTreeManager ctm = CollisionTreeManager.getInstance();
	
	protected void simpleInitGame() {
		// Kamera Position
		cam.setLocation(new Vector3f(50,50,150));
		// Skybox erstellen
		wc = new World();
		
		// Schwarm initialisieren
		schwarm = new SchwarmController();
		schwarm.addFlies(50);
		schwarm.setWorld(wc);
		Node n = schwarm.getSwarmNode();
		rootNode.attachChild(n);
		rootNode.attachChild(wc.getWorldRootNode());
		rootNode.attachChild(schwarm.getLeittierNode());
		//ctm.generateCollisionTree(CollisionTree.Type.Sphere, n, true);
		
		/*
		PointLight pl = new PointLight();
		pl.setLocation(schwarm.getLeittierNode().getLocalTranslation());
		pl.setEnabled(true);
		pl.setDiffuse(ColorRGBA.red);
		lightState.attach(pl);
		*/
	}
	public static void main(String[] args) {
		new Main().start();
	}
	
	protected void simpleUpdate(){
		//schwarm.setLeittier(pc.getPosition());
		schwarm.updateAll();
		
	}
	
}