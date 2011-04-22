package ephemera.gui;

/*
 * Copyright (c) 2003-2009 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.canvas.JMECanvas;
import com.jme.system.canvas.SimpleCanvasImpl;
import com.jme.system.lwjgl.LWJGLSystemProvider;
import com.jme.util.Debug;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;

import com.jme.util.stat.StatCollector;

import com.jmex.awt.lwjgl.LWJGLAWTCanvasConstructor;
import com.jmex.effects.particles.ParticleSystem;

/**
 * <code>RenParticleControlFrame</code>
 * 
 * @author Joshua Slack
 * @author Andrzej Kapolka - additions for multiple layers, save/load from jme
 *         format
 * @version $Id: RenParticleEditor.java 4130 2009-03-19 20:04:51Z blaine.dev $
 */

public class RenParticleEditor extends JFrame {
	
	
	


    public static Node particleNode;
    public static ParticleSystem particleGeom;

    private static final long serialVersionUID = 1L;

    int width = 640, height = 480;

    MyImplementor impl;
    private CameraHandler camhand;
    private Canvas glCanvas;
    private Node root;
    private Geometry grid;
    private Action spawnAction;

    // edit panels

    // layer panel components
    private JButton deleteLayerButton;
    private JButton TestButton;


    // examples panel components

    private File openFile;

    private Preferences prefs = Preferences
            .userNodeForPackage(RenParticleEditor.class);

    private JCheckBoxMenuItem yUp;

    private JCheckBoxMenuItem zUp;

    
    
    /**
     * Main Entry point...
     * 
     * @param args
     *            String[]
     */
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable() {

    		public void run() {
    			try {
    				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    			} catch (Exception e) {
    				//Hier Fehlermeldung bzw logger
    			}
    			new RenParticleEditor();
    		}});
    }

    public RenParticleEditor() {
        try {
            init();
            // center the frame
            setLocationRelativeTo(null);

            // show frame
            setVisible(true);

            // init some location dependent sub frames

            while (glCanvas == null) {
            	try { Thread.sleep(100); } catch (InterruptedException e) {}
            }

        } catch (Exception ex) {
        }
    }

    private void init() throws Exception {
        updateTitle();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFont(new Font("Arial", 0, 12));

        setJMenuBar(createMenuBar());
     
        //3D view ----------------------------------------------
        JPanel canvasPanel = new JPanel();
        canvasPanel.setLayout(new BorderLayout());
        canvasPanel.add(getGlCanvas(), BorderLayout.CENTER);
        Dimension minimumSize = new Dimension(150, 150);
        canvasPanel.setMinimumSize(minimumSize);
        
        //interface ---------------------------------------------
        JPanel interfacePanel = new JPanel();
        interfacePanel.setLayout(new BorderLayout());
        interfacePanel.add(createLayerPanel());
        

        //linke seite unterteilen
       // JSplitPane sideSplit = new JSplitPane();
       // sideSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
       // sideSplit.setTopComponent(createLayerPanel());
       // sideSplit.setDividerLocation(150);

        
        //Bildschirm unterteilen in interface und 3D view
        JSplitPane mainSplit = new JSplitPane();
        mainSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setLeftComponent(interfacePanel);
        mainSplit.setRightComponent(canvasPanel);
        mainSplit.setDividerLocation(300);
        getContentPane().add(mainSplit, BorderLayout.CENTER);

        grid = createGrid();
        
        yUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Callable<Void> exe = new Callable<Void>() {
                    public Void call() {
                        camhand.worldUpVector.set(Vector3f.UNIT_Y);
                        Camera cam = impl.getRenderer().getCamera();
                        cam.getLocation().set(0, 850, -850);
                        camhand.recenterCamera();
                        grid.unlock();
                        grid.getLocalRotation().fromAngleAxis(0, Vector3f.UNIT_X);
                        grid.lock();
                        prefs.putBoolean("yUp", true);
                        return null;
                    }
                };
                GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
                        .enqueue(exe);
            }
        });
        zUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Callable<Void> exe = new Callable<Void>() {
                    public Void call() {
                        camhand.worldUpVector.set(Vector3f.UNIT_Z);
                        Camera cam = impl.getRenderer().getCamera();
                        cam.getLocation().set(0, -850, 850);
                        camhand.recenterCamera();
                        grid.unlock();
                        grid.getLocalRotation().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
                        grid.lock();
                        prefs.putBoolean("yUp", false);
                        return null;
                    }
                };
                GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
                        .enqueue(exe);
            }
        });
        
        Callable<Void> exe = new Callable<Void>() {
            public Void call() {
                if (prefs.getBoolean("yUp", true)) {
                    yUp.doClick();
                } else {
                    zUp.doClick();
                }
                return null;
            }
        };
        GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
                .enqueue(exe);

        setSize(new Dimension(1024, 768));
    }

    private void updateTitle() {
        setTitle("Particle System Editor"
                + (openFile == null ? "" : (" - " + openFile)));
    }

    private JMenuBar createMenuBar() {
        Action newAction = new AbstractAction("New") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                createNewSystem();
            }
        };
        newAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);

        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        file.add(newAction);
    

        spawnAction = new AbstractAction("Force Spawn") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                for (Spatial child : particleNode.getChildren()) {
                    if (child instanceof ParticleSystem) {
                        ((ParticleSystem) child).forceRespawn();
                    }
                }
            }
        };
        spawnAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
        spawnAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_F, 0));

        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        edit.add(spawnAction);

        Action showGrid = new AbstractAction("Show Grid") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                grid
                        .setCullHint(grid.getCullHint() == Spatial.CullHint.Always ? Spatial.CullHint.Dynamic
                                : Spatial.CullHint.Always);
                prefs.putBoolean("showgrid", grid.getCullHint() != Spatial.CullHint.Always);
            }
        };
        showGrid.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);

      

        Action recenter = new AbstractAction("Recenter Camera") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                camhand.recenterCamera();
            }
        };
        recenter.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);

        yUp = new JCheckBoxMenuItem("Y-Up Camera");
        yUp.setMnemonic(KeyEvent.VK_Y);
        zUp = new JCheckBoxMenuItem("Z-Up Camera");
        zUp.setMnemonic(KeyEvent.VK_Y);
        ButtonGroup upGroup = new ButtonGroup();
        upGroup.add(yUp);
        upGroup.add(zUp);

        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        JCheckBoxMenuItem sgitem = new JCheckBoxMenuItem(showGrid);
        sgitem.setSelected(prefs.getBoolean("showgrid", true));
        view.add(sgitem);
        view.addSeparator();
        view.add(recenter);
        view.add(yUp);
        view.add(zUp);

        JMenuBar mbar = new JMenuBar();
        mbar.add(file);
        mbar.add(edit);
        mbar.add(view);
        return mbar;
    }

    
    
    // JPanel, hier werden Buttons etc hinzugef�gt allerdings in das "obere" menue (eben: delete & new button)
    private JPanel createLayerPanel() {

    	
    	// Button NEW ------------------------------------------------------
        JButton newLayerButton = new JButton(new AbstractAction("New") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                
            }
        });
        newLayerButton.setMargin(new Insets(2, 14, 2, 14));

        
    	// Button DELETE ------------------------------------------------------
        deleteLayerButton = new JButton(new AbstractAction("Delete") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
              //  deleteLayer();
            }
        });
        deleteLayerButton.setMargin(new Insets(2, 14, 2, 14));
        deleteLayerButton.setEnabled(false);

        
        //Test button ---------------------------------------------------------
        TestButton = new JButton(new AbstractAction("put some shit") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
              //  deleteLayer();
            }
        });
        TestButton.setMargin(new Insets(2, 14, 2, 14));
        TestButton.setEnabled(true);
        
        
        //Grid ----------------------------------------------------------------
        JPanel layerPanel = new JPanel(new GridBagLayout());

        
        // Buttons zum layerPanel Hinzuf�gen ----------------------------------
        layerPanel.add(newLayerButton, new GridBagConstraints(0, 2, 1, 1, 0.5,
                0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 10, 10, 10), 0, 0));
        layerPanel.add(deleteLayerButton, new GridBagConstraints(1, 2, 1, 1,
                0.5, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 10, 10, 10), 0, 0));
        layerPanel.add(TestButton, new GridBagConstraints(0, 3, 1, 2,
                0.5, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(5, 10, 10, 10), 0, 0));
        return layerPanel;
    }

    

 

    private void createNewSystem() {
        particleNode.detachAllChildren();
        deleteLayerButton.setEnabled(false);
        openFile = null;
        updateTitle();
    }


    /**
     * updateFromManager
     */
  

    /**
     * updateManager
     * 
     * @param particles
     *            number of particles to reset manager with.
     */
 

    private ColorRGBA makeColorRGBA(Color color) {
        return new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, color.getAlpha() / 255f);
    }



    protected Canvas getGlCanvas() {
        if (glCanvas == null) {

            // -------------GL STUFF------------------

            // make the canvas:
        	DisplaySystem display = DisplaySystem.getDisplaySystem(LWJGLSystemProvider.LWJGL_SYSTEM_IDENTIFIER);
        	display.registerCanvasConstructor("AWT", LWJGLAWTCanvasConstructor.class);
            glCanvas = (Canvas)display.createCanvas(width, height);
            glCanvas.setMinimumSize(new Dimension(100, 100));

            // add a listener... if window is resized, we can do something about
            // it.
            glCanvas.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent ce) {
                    doResize();
                }
            });

            camhand = new CameraHandler();

            glCanvas.addMouseWheelListener(camhand);
            glCanvas.addMouseListener(camhand);
            glCanvas.addMouseMotionListener(camhand);

            // Important! Here is where we add the guts to the canvas:
            impl = new MyImplementor(width, height);

            ((JMECanvas) glCanvas).setImplementor(impl);

            // -----------END OF GL STUFF-------------

            Callable<Void> exe = new Callable<Void>() {
                public Void call() {
                    forceUpdateToSize();
                    ((JMECanvas) glCanvas).setTargetRate(60);
                    return null;
                }
            };
            GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).enqueue(exe);
        }
        return glCanvas;
    }

    public void forceUpdateToSize() {
        // force a resize to ensure proper canvas size.
        glCanvas.setSize(glCanvas.getWidth(), glCanvas.getHeight() + 1);
        glCanvas.setSize(glCanvas.getWidth(), glCanvas.getHeight() - 1);
    }

    class CameraHandler extends MouseAdapter implements MouseMotionListener,
            MouseWheelListener {
        Point last = new Point(0, 0);
        Vector3f focus = new Vector3f();
        private Vector3f vector = new Vector3f();
        private Quaternion rot = new Quaternion();
        public Vector3f worldUpVector = Vector3f.UNIT_Y.clone();

        public void mouseDragged(final MouseEvent arg0) {
            Callable<Void> exe = new Callable<Void>() {
                public Void call() {
                    int difX = last.x - arg0.getX();
                    int difY = last.y - arg0.getY();
                    int mult = arg0.isShiftDown() ? 10 : 1;
                    last.x = arg0.getX();
                    last.y = arg0.getY();

                    int mods = arg0.getModifiers();
                    if ((mods & InputEvent.BUTTON1_MASK) != 0) {
                        rotateCamera(worldUpVector, difX * 0.0025f);
                        rotateCamera(impl.getRenderer().getCamera().getLeft(),
                                -difY * 0.0025f);
                    }
                    if ((mods & InputEvent.BUTTON2_MASK) != 0 && difY != 0) {
                        zoomCamera(difY * mult);
                    }
                    if ((mods & InputEvent.BUTTON3_MASK) != 0) {
                        panCamera(-difX, -difY);
                    }
                    return null;
                }
            };
            GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
                    .enqueue(exe);
        }

        public void mouseMoved(MouseEvent arg0) {
        }

        public void mousePressed(MouseEvent arg0) {
            last.x = arg0.getX();
            last.y = arg0.getY();
        }

        public void mouseWheelMoved(final MouseWheelEvent arg0) {
            Callable<Void> exe = new Callable<Void>() {
                public Void call() {
                    zoomCamera(arg0.getWheelRotation()
                            * (arg0.isShiftDown() ? -100 : -20));
                    return null;
                }
            };
            GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
                    .enqueue(exe);
        }

        public void recenterCamera() {
            Callable<Void> exe = new Callable<Void>() {
                public Void call() {
                    Camera cam = impl.getRenderer().getCamera();
                    Vector3f.ZERO.subtract(focus, vector);
                    cam.getLocation().addLocal(vector);
                    focus.addLocal(vector);
                    cam.lookAt(focus, worldUpVector );
                    cam.onFrameChange();
                    return null;
                }
            };
            GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER)
                    .enqueue(exe);
        }

        private void rotateCamera(Vector3f axis, float amount) {
            Camera cam = impl.getRenderer().getCamera();
            if (axis.equals(cam.getLeft())) {
                float elevation = -FastMath.asin(cam.getDirection().z);
                // keep the camera constrained to -89 -> 89 degrees elevation
                amount = Math.min(Math.max(elevation + amount,
                        -(FastMath.DEG_TO_RAD * 89)), (FastMath.DEG_TO_RAD * 89))
                        - elevation;
            }
            rot.fromAngleAxis(amount, axis);
            cam.getLocation().subtract(focus, vector);
            rot.mult(vector, vector);
            focus.add(vector, cam.getLocation());
            cam.lookAt(focus, worldUpVector );
        }

        private void panCamera(float left, float up) {
            Camera cam = impl.getRenderer().getCamera();
            cam.getLeft().mult(left, vector);
            vector.scaleAdd(up, cam.getUp(), vector);
            cam.getLocation().addLocal(vector);
            focus.addLocal(vector);
            cam.onFrameChange();
        }

        private void zoomCamera(float amount) {
            Camera cam = impl.getRenderer().getCamera();
            float dist = cam.getLocation().distance(focus);
            amount = dist - Math.max(0f, dist - amount);
            cam.getLocation().scaleAdd(amount, cam.getDirection(),
                    cam.getLocation());
            cam.onFrameChange();
        }
    }

    protected void doResize() {
        if (impl != null) {
            impl.resizeCanvas(glCanvas.getWidth(), glCanvas.getHeight());
            if (impl.getCamera() != null) {
                Callable<Void> exe = new Callable<Void>() {
                    public Void call() {
                        impl.getCamera().setFrustumPerspective(
                                45.0f,
                                (float) glCanvas.getWidth()
                                        / (float) glCanvas.getHeight(), 1,
                                10000);
                        return null;
                    }
                };
                GameTaskQueueManager.getManager()
                        .getQueue(GameTaskQueue.RENDER).enqueue(exe);
            }
        }
    }

    
    

    // IMPLEMENTING THE SCENE: --------------------------------------------------------------------

    class MyImplementor extends SimpleCanvasImpl {

        /**
         * The root node of our stat graphs.
         */
        protected Node statNode;


        private Quad labGraph;
        public MyImplementor(int width, int height) {
            super(width, height);
        }

        
        //3D ged�ns
        public void simpleSetup() {
            Color bg = new Color(prefs.getInt("bg_color", 0));
            renderer.setBackgroundColor(makeColorRGBA(bg));
            cam.setFrustumPerspective(45.0f, (float) glCanvas.getWidth()
                    / (float) glCanvas.getHeight(), 1, 10000);

            root = rootNode;

            // Finally, a stand alone node (not attached to root on purpose)
            statNode = new Node("stat node");
            statNode.setCullHint(Spatial.CullHint.Never);

            root.attachChild(grid);
            grid.updateRenderState();

            particleNode = new Node("particles");
            root.attachChild(particleNode);

            ZBufferState zbuf = renderer.createZBufferState();
            zbuf.setWritable(false);
            zbuf.setEnabled(true);
            zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

            particleNode.setRenderState(zbuf);
            particleNode.updateRenderState();

            statNode.updateGeometricState(0, true);
            statNode.updateRenderState();

            createNewSystem();
          

        };

        
        
        
        
        public void simpleUpdate() {
            if (Debug.stats) {
                StatCollector.update();
                labGraph.setLocalTranslation((renderer.getWidth()-.5f*labGraph.getWidth()), (renderer.getHeight()-.5f*labGraph.getHeight()), 0);
            }
        }

        
        
        @Override
        public void simpleRender() {
            statNode.draw(renderer);
        }

        
        
        
        
        
        /**
         * Set up which stats to graph
         *
         */
      
        
        
        
        /**
         * Set up the graphers we will use and the quads we'll show the stats on.
         *
         */
        
    }

    
    
    
    private static final int GRID_LINES = 51;
    private static final float GRID_SPACING = 100f;

    private Geometry createGrid() {
        Vector3f[] vertices = new Vector3f[GRID_LINES * 2 * 2];
        float edge = GRID_LINES / 2 * GRID_SPACING;
        for (int ii = 0, idx = 0; ii < GRID_LINES; ii++) {
            float coord = (ii - GRID_LINES / 2) * GRID_SPACING;
            vertices[idx++] = new Vector3f(-edge, 0f, coord);
            vertices[idx++] = new Vector3f(+edge, 0f, coord);
            vertices[idx++] = new Vector3f(coord, 0f, -edge);
            vertices[idx++] = new Vector3f(coord, 0f, +edge);
        }
        Geometry grid = new com.jme.scene.Line("grid", vertices, null,
                null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public void draw(Renderer r) {
                StatCollector.pause();
                super.draw(r);
                StatCollector.resume();
            }
        };
        grid.getDefaultColor().set(ColorRGBA.darkGray.clone());
        grid.setCullHint(prefs.getBoolean("showgrid", true) ? Spatial.CullHint.Dynamic
                : Spatial.CullHint.Always);
        return grid;
    }
}