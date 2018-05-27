package screenshareserver;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Principal extends javax.swing.JFrame {

    ArrayList<Cliente> listaClientes = new ArrayList<>();

    class Cliente {
        Socket s;
        int h, w;

        public Cliente(Socket s, int h, int w) {
            this.s = s;
            this.h = h;
            this.w = w;
        }
    }
    public Principal() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTAStatus = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jTFTempo = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("Iniciar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTAStatus.setEditable(false);
        jTAStatus.setColumns(20);
        jTAStatus.setRows(5);
        jScrollPane1.setViewportView(jTAStatus);

        jLabel1.setText("Tempo entre frames:");

        jTFTempo.setText("10");

        jLabel2.setText("ms");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTFTempo, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jLabel1)
                    .addComponent(jTFTempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    class ThreadServidor extends Thread {

        public void run() {
            try {
                ServerSocket server;
                server = new ServerSocket(1234);
                jTAStatus.append("Thread aguardando conexões criada\n");
                while (true) {
                    Socket cli;
                    cli = server.accept();
                    jTAStatus.append("Conexão de cliente recebida\n");
                    DataInputStream dis = new DataInputStream(cli.getInputStream());
                    int h = dis.readInt();
                    int w = dis.readInt();
                    /*h = (int) (h * 0.9);
                    w = (int) (w * 0.9);*/
                    Cliente c = new Cliente(cli, h, w);
                    listaClientes.add(c);
                    jTAStatus.append("Cliente "+h+" x "+w+" adicionado na lista\n");
                }
            } catch (Exception e) {

            }
        }
    }

    public boolean enviaImagem(Cliente cli, BufferedImage imagem) {
        try {
            BufferedImage imagemResize= resize(imagem, cli.w, cli.h);
            OutputStream os = cli.s.getOutputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imagemResize, "png", baos);
            baos.close();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(baos.size() + "");
            os.write(baos.toByteArray());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    class ThreadScreenShot extends Thread {

        public void run() {
            jTAStatus.append("Thread de screenshots criada\n");
            long tempo = 0;
            try {
                tempo = Long.parseLong(jTFTempo.getText());
            } catch (Exception e) {
                tempo = 10;
            }
            while (true) {
                try {
                    BufferedImage img = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                    for (Cliente cli : listaClientes) {
                        if (!enviaImagem(cli, img)) {
                            listaClientes.remove(cli);
                            jTAStatus.append("Cliente removido por falha\n");
                            break;
                        }
                    }
                    sleep(tempo);
                } catch (AWTException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //cria uma thread que fica tirando screenshots a cada x tempo
        ThreadScreenShot tss = new ThreadScreenShot();
        tss.start();
        //cria a thread que vai ficar aguardando clientes se conectarem
        ThreadServidor tserv = new ThreadServidor();
        tserv.start();

    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTAStatus;
    private javax.swing.JTextField jTFTempo;
    // End of variables declaration//GEN-END:variables
}
