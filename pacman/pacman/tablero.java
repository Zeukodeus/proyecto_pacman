package pacman;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class tablero extends JPanel implements ActionListener {
    
    // Dimensiones del tablero
    private Dimension d;
    
    // Fuente para mostrar puntaje
    private final Font smallfont = new Font("Helvetica", Font.BOLD, 14);
    
    // Imágenes de los personajes y fantasmas
    private Image ii;
    private final Color dotcolor = new Color(192, 192, 0);
    private Color mazecolor;
    
    // Estado del juego
    private boolean ingame = false;
    private boolean dying = false;
    
    // Tamaño del bloque, número de bloques, tamaño de la pantalla, velocidad del pacman, etc.
    private final int blocksize = 24;
    private final int nrofblocks = 15;
    private final int scrsize = nrofblocks * blocksize;
    private final int pacanimdelay = 2;
    private final int pacmananimcount = 4;
    
    // Número máximo de fantasmas
    private final int maxghosts = 12;
    private final int pacmanspeed = 6;

    // Animaciones del pacman
    private int pacanimcount = pacanimdelay;
    private int pacanimdir = 1;
    private int pacmananimpos = 0;
    private int nrofghosts = 6;
    
    // Variables relacionadas con el puntaje y vidas
    private int pacsleft, score;
    
    // Arreglos para almacenar las posiciones y velocidades de los fantasmas y del pacman
    private int[] dx, dy;
    private int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;

    // Imágenes de los personajes y fantasmas en diferentes direcciones
    private Image ghost;
    private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    private Image pacman3up, pacman3down, pacman3left, pacman3right;
    private Image pacman4up, pacman4down, pacman4left, pacman4right;

    // Posición y dirección actual del pacman
    private int pacmanx, pacmany, pacmandx, pacmandy;
    
    // Dirección solicitada por el usuario
    private int reqdx, reqdy, viewdx, viewdy;

    // Datos del nivel (laberinto)
    private final short leveldata[] = {
        19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
        17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
        25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
        1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
        1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
        1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
        1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
        9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };

    // Velocidades válidas y máxima velocidad
    private final int validspeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxspeed = 6;

    // Velocidad actual
    private int currentspeed = 3;
    
    // Datos del laberinto para cada posición
    private short[] screendata;
    
    // Temporizador para el bucle del juego
    private Timer timer;

    // Constructor de la clase
    public tablero() {

        loadImages();
        initVariables();

        // Agregar un KeyListener para capturar eventos del teclado
        addKeyListener(new TAdapter());

        setFocusable(true);

        setBackground(Color.BLACK);
        setDoubleBuffered(true);
    }

    // Inicialización de variables
    private void initVariables() {

        screendata = new short[nrofblocks * nrofblocks];
        mazecolor = new Color(5, 100, 5);
        d = new Dimension(400, 400);
        ghostx = new int[maxghosts];
        ghostdx = new int[maxghosts];
        ghosty = new int[maxghosts];
        ghostdy = new int[maxghosts];
        ghostspeed = new int[maxghosts];
        dx = new int[4];
        dy = new int[4];

        // Iniciar el temporizador para el bucle del juego
        timer = new Timer(40, this);
        timer.start();
    }

    // Se llama cuando se agrega el componente al contenedor
    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }

    // Animación del pacman
    private void doAnim() {

        pacanimcount--;

        if (pacanimcount <= 0) {
            pacanimcount = pacanimdelay;
            pacmananimpos = pacmananimpos + pacanimdir;

            if (pacmananimpos == (pacmananimcount - 1) || pacmananimpos == 0) {
                pacanimdir = -pacanimdir;
            }
        }
    }

    // Función principal para el manejo del juego
    private void playGame(Graphics2D g2d) {

        if (dying) {

             // Si el pacman está muriendo
            death();

        } else {

            // Si el pacman está vivo
            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    // Pantalla de introducción antes de comenzar el juego
    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);

        String s = "Presiona Enter para empezar.";
        Font small = new Font("Gabriola", Font.BOLD, 25);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
    }

    // Mostrar el puntaje en la pantalla
    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallfont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, scrsize / 2 + 96, scrsize + 16);

        for (i = 0; i < pacsleft; i++) {
            g.drawImage(pacman3left, i * 28 + 8, scrsize + 1, this);
        }
    }

    // Verificar si se ha completado el laberinto
    private void checkMaze() {

        short i = 0;
        boolean finished = true;

        while (i < nrofblocks * nrofblocks && finished) {

            if ((screendata[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            // Si se ha completado el laberinto
            score += 50;

            if (nrofghosts < maxghosts) {
                nrofghosts++;
            }

            if (currentspeed < maxspeed) {
                currentspeed++;
            }

            initLevel();
        }
    }

    // Función llamada cuando el pacman muere
    private void death() {
        pacsleft--;

        if (pacsleft == 0) {
            ingame = false;
        }

        continueLevel();
    }

    // Mover a los fantasmas
    private void moveGhosts(Graphics2D g2d) {

        short i;
        int pos;
        int count;

        for (i = 0; i < nrofghosts; i++) {
            if (ghostx[i] % blocksize == 0 && ghosty[i] % blocksize == 0) {
                pos = ghostx[i] / blocksize + nrofblocks * (int) (ghosty[i] / blocksize);

                count = 0;

                if ((screendata[pos] & 1) == 0 && ghostdx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screendata[pos] & 2) == 0 && ghostdy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screendata[pos] & 4) == 0 && ghostdx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screendata[pos] & 8) == 0 && ghostdy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screendata[pos] & 15) == 15) {
                        ghostdx[i] = 0;
                        ghostdy[i] = 0;
                    } else {
                        ghostdx[i] = -ghostdx[i];
                        ghostdy[i] = -ghostdy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghostdx[i] = dx[count];
                    ghostdy[i] = dy[count];
                }

            }

            ghostx[i] = ghostx[i] + (ghostdx[i] * ghostspeed[i]);
            ghosty[i] = ghosty[i] + (ghostdy[i] * ghostspeed[i]);
            drawGhost(g2d, ghostx[i] + 1, ghosty[i] + 1);

            if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12)
                    && pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12)
                    && ingame) {

                dying = true;
            }
        }
    }

    // Dibujar el fantasma en la posición dada
    private void drawGhost(Graphics2D g2d, int x, int y) {

        g2d.drawImage(ghost, x, y, this);
    }

    // Mover al pacman
    private void movePacman() {

        int pos;
        short ch;

        if (reqdx == -pacmandx && reqdy == -pacmandy) {
            pacmandx = reqdx;
            pacmandy = reqdy;
            viewdx = pacmandx;
            viewdy = pacmandy;
        }

        if (pacmanx % blocksize == 0 && pacmany % blocksize == 0) {
            pos = pacmanx / blocksize + nrofblocks * (int) (pacmany / blocksize);
            ch = screendata[pos];

            if ((ch & 16) != 0) {
                screendata[pos] = (short) (ch & 15);
                score++;
            }

            if (reqdx != 0 || reqdy != 0) {
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0)
                        || (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
                        || (reqdx == 0 && reqdy == -1 && (ch & 2) != 0)
                        || (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
                    pacmandx = reqdx;
                    pacmandy = reqdy;
                    viewdx = pacmandx;
                    viewdy = pacmandy;
                }
            }

            // Verificar la quietud del pacman
            if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0)
                    || (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0)
                    || (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0)
                    || (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
                pacmandx = 0;
                pacmandy = 0;
            }
        }
        pacmanx = pacmanx + pacmanspeed * pacmandx;
        pacmany = pacmany + pacmanspeed * pacmandy;
    }

    // Dibujar al pacman según su dirección
    private void drawPacman(Graphics2D g2d) {

        if (viewdx == -1) {
            drawPacnanLeft(g2d);
        } else if (viewdx == 1) {
            drawPacmanRight(g2d);
        } else if (viewdy == -1) {
            drawPacmanUp(g2d);
        } else {
            drawPacmanDown(g2d);
        }
    }

    // Dibujar el pacman y sus animaciones en dirección hacia arriba
    private void drawPacmanUp(Graphics2D g2d) {

        switch (pacmananimpos) {
            case 1:
                g2d.drawImage(pacman2up, pacmanx + 1, pacmany + 1, this);
        
                break;
            case 2:
                g2d.drawImage(pacman3up, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

    // Dibujar el pacman y sus animaciones en dirección hacia abajo
    private void drawPacmanDown(Graphics2D g2d) {

        switch (pacmananimpos) {
            case 1:
                g2d.drawImage(pacman2down, pacmanx + 1, pacmany + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3down, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

    // Dibujar el pacman y sus animaciones en dirección hacia la izquierda
    private void drawPacnanLeft(Graphics2D g2d) {

        switch (pacmananimpos) {
            case 1:
                g2d.drawImage(pacman2left, pacmanx + 1, pacmany + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3left, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

    // Dibujar el pacman y sus animaciones en dirección hacia la derecha
    private void drawPacmanRight(Graphics2D g2d) {

        switch (pacmananimpos) {
            case 1:
                g2d.drawImage(pacman2right, pacmanx + 1, pacmany + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3right, pacmanx + 1, pacmany + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
                break;
        }
    }

    // Dibujar el laberinto
    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < scrsize; y += blocksize) {
            for (x = 0; x < scrsize; x += blocksize) {

                g2d.setColor(mazecolor);
                g2d.setStroke(new BasicStroke(2));

                if ((screendata[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + blocksize - 1);
                }

                if ((screendata[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + blocksize - 1, y);
                }

                if ((screendata[i] & 4) != 0) {
                    g2d.drawLine(x + blocksize - 1, y, x + blocksize - 1,
                            y + blocksize - 1);
                }

                if ((screendata[i] & 8) != 0) {
                    g2d.drawLine(x, y + blocksize - 1, x + blocksize - 1,
                            y + blocksize - 1);
                }

                if ((screendata[i] & 16) != 0) {
                    g2d.setColor(dotcolor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                i++;
            }
        }
    }

    // Inicializar el juego
    private void initGame() {

        pacsleft = 3;
        score = 0;
        initLevel();
        nrofghosts = 6;
        currentspeed = 3;
    }

    // Inicializar el nivel (laberinto)
    private void initLevel() {

        int i;
        for (i = 0; i < nrofblocks * nrofblocks; i++) {
            screendata[i] = leveldata[i];
        }

        continueLevel();
    }

    // Continuar el juego después de completar un nivel
    private void continueLevel() {

        short i;
        int dx = 1;
        int random;

        for (i = 0; i < nrofghosts; i++) {

            ghosty[i] = 4 * blocksize;
            ghostx[i] = 4 * blocksize;
            ghostdy[i] = 0;
            ghostdx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentspeed + 1));

            if (random > currentspeed) {
                random = currentspeed;
            }

            ghostspeed[i] = validspeeds[random];
        }

        pacmanx = 7 * blocksize;
        pacmany = 11 * blocksize;
        pacmandx = 0;
        pacmandy = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
    }

    // Cargar imágenes del juego
    private void loadImages() {

        ghost = new ImageIcon(getClass().getResource("../images/ghost.gif")).getImage();
        pacman1 = new ImageIcon(getClass().getResource("../images/pacman.gif")).getImage();
        pacman2up = new ImageIcon(getClass().getResource("../images/up1.gif")).getImage();
        pacman3up = new ImageIcon(getClass().getResource("../images/up2.gif")).getImage();
        pacman4up = new ImageIcon(getClass().getResource("../images/up3.gif")).getImage();
        pacman2down = new ImageIcon(getClass().getResource("../images/down1.gif")).getImage();
        pacman3down = new ImageIcon(getClass().getResource("../images/down2.gif")).getImage();
        pacman4down = new ImageIcon(getClass().getResource("../images/down3.gif")).getImage();
        pacman2left = new ImageIcon(getClass().getResource("../images/left1.gif")).getImage();
        pacman3left = new ImageIcon(getClass().getResource("../images/left2.gif")).getImage();
        pacman4left = new ImageIcon(getClass().getResource("../images/left3.gif")).getImage();
        pacman2right = new ImageIcon(getClass().getResource("../images/right1.gif")).getImage();
        pacman3right = new ImageIcon(getClass().getResource("../images/right2.gif")).getImage();
        pacman4right = new ImageIcon(getClass().getResource("../images/right3.gif")).getImage();

    }

    // Dibujar el laberinto y los personajes
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    // Dibujar componentes del juego
    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        doAnim();

        if (ingame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    // Clase interna para manejar los eventos del teclado
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (ingame) {
            //Direccion del teclado.
                if (key == KeyEvent.VK_LEFT) {
                    reqdx = -1;
                    reqdy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    reqdx = 1;
                    reqdy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    reqdx = 0;
                    reqdy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    reqdx = 0;
                    reqdy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    ingame = false;
                //Pausar el Juego con la P.
                } else if (key == 'p' || key == 'P') {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            //Iniciar el juego con ENTER.
            } else {
                if (key == KeyEvent.VK_ENTER) {
                    ingame = true;
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                reqdx = 0;
                reqdy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}

