/**
 * ShitController by Kilian Heinrich
 * diese Klasse verwaltet die eigenschaften eines Magnetischen Objektes "Schei�e" 
 * welches in die Welt hinzugefuegt werden kann.
 */


package ephemera.controller;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Controller;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
import com.jme.system.lwjgl.LWJGLSystemProvider;

import ephemera.view.MyJmeView;



public class ShitController {

	
	private WorldController worldController;
	MaterialState shitMaterial;
	DisplaySystem display = DisplaySystem.getDisplaySystem(LWJGLSystemProvider.LWJGL_SYSTEM_IDENTIFIER);
	MyJmeView impl ;
	
	
	public ShitController(WorldController worldController){
		this.worldController = worldController;
	}
	
	
	
	
	public void mouseShot() {
    		
		System.out.println("boom");	
		/** Material erstellen */
		shitMaterial = display.getRenderer().createMaterialState();
		shitMaterial.setEmissive(ColorRGBA.brown.clone());
    	
	
    	
		/**anz Shits*/
		int numShits = 0;
		
		Camera cam = impl.getRenderer().getCamera();

		Sphere shit = new Sphere("bullet" + numShits++, 8, 8, 2);
		shit.setModelBound(new BoundingSphere());
		shit.updateModelBound();
	
		/** An Camera Position bewegen */
		shit.setLocalTranslation(new Vector3f(cam.getLocation()));
		shit.setRenderState(shitMaterial);

		/**
		 * Update in worldcontroller
		 */
		shit.updateGeometricState(0, true);
	
		/**
		 * BewegungsController hinzufuegen "ShitMover"
		 */
		shit.addController(new ShitMover(shit, new Vector3f(cam.getDirection())));
		worldController.getWorldRootNode().attachChild(shit);
		shit.updateRenderState();
			
		
	}
    
    class ShitMover extends Controller {
		private static final long serialVersionUID = 1L;
		
			TriMesh shit;
 
			/** Richtung des Objektes */
			Vector3f direction;
 
			/** Geschwindigkeit */
			float speed = 100;
		
			/**Lebenszeit*/
			float lifeTime = 20;
 
			ShitMover(TriMesh bullet, Vector3f direction) {
				this.shit = bullet;
				this.direction = direction;
				this.direction.normalizeLocal();
		}
 
		public void update(float time) {
			lifeTime -= time;
			
			
			/**
			 * Flubahn
			 */
			//kommt noch
			
			
			
			/** Entfernen wenn "lebenszeit" vorbei */
			if (lifeTime < 0) {
				worldController.getWorldRootNode().detachChild(shit);
				shit.removeController(this);
				return;
			}
			
			/** Bewege an Position */
			Vector3f shitPos = shit.getLocalTranslation();
			shitPos.addLocal(direction.mult(time * speed));
			shit.setLocalTranslation(shitPos);
			
			
			/**
			 * Kollisionsabfrage
			 */
			if (shit.getWorldBound().intersects(worldController.getWorldRootNode().getWorldBound())) {

				System.out.println("baaaaaaam");
				speed = 0;
 
			}
		
			
		}
	}
	
}