// Importación de paquetes necesarios
package pacman;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Definición de la clase Pacman que hereda de JFrame (ventana gráfica)
public class Pacman extends JFrame {

    // Constructor de la clase Pacman
    public Pacman() {
        // Inicialización de la interfaz de usuario
        initUI();
    }
    
    // Método privado para inicializar la interfaz de usuario
    private void initUI() {
        // Agrega un nuevo objeto de la clase "tablero" a la ventana
        add(new tablero());
        
        // Configuración del título de la ventana
        setTitle("Proyecto PacMan");
        
        // Configuración para cerrar la aplicación al cerrar la ventana
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Agregar un WindowListener para gestionar el cierre de la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });
        
        // Configuración del tamaño de la ventana
        setSize(380, 420);
        
        // Configuración de la posición inicial de la ventana (centrada)
        setLocationRelativeTo(null);
        
        // Hace visible la ventana
        setVisible(true);        
    }

    // Método para confirmar la salida
    private void confirmarSalida() {
        int option = JOptionPane.showConfirmDialog(this, "¿Estás seguro de salir?", "Confirmar salida", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            // Si el usuario elige "Sí", cierra la aplicación
            System.exit(0);
        }
        // Si el usuario elige "No" o cierra el cuadro de diálogo, la aplicación continúa ejecutándose
    }

    // Método principal (punto de entrada) de la aplicación
    public static void main(String[] args) {
        // Invocación de la interfaz de usuario en el hilo de despacho de eventos
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Creación de una instancia de la clase Pacman
                Pacman ex = new Pacman();
                
                // Hace visible la instancia creada
                ex.setVisible(true);
            }
        });
    }
}