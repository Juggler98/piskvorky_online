import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Piskvorky extends JPanel implements MouseListener {

    public static final int velkostPolicka = 128;
    public static final int rozmer = 3;

    private final int[][] piskvorky = new int[rozmer][rozmer];
    private final String[] hraci = {"X", "O"};
    private int vyhralHrac = 0;
    private int tah = 0;
    private boolean koniecHry = false;

    private int me;
    private int lastMove = 0;

    private final static FirebaseService firebaseService = new FirebaseService();
    private final static Firestore db = firebaseService.getDb();

    private String gameId;

    Piskvorky() {

    }

    private void init() {
        for (int y = 0; y < rozmer; y++) {
            for (int x = 0; x < rozmer; x++) {
                piskvorky[y][x] = 0;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int x = 1; x < rozmer; x++) {
            for (int y = 0; y < rozmer; y++) {
                ImageIcon verticalIcon = new ImageIcon("assets/vertical_line.png");
                g.drawImage(verticalIcon.getImage(), x * velkostPolicka - velkostPolicka / 2, y * velkostPolicka, this);
            }
        }
        for (int y = 1; y < rozmer; y++) {
            for (int x = 0; x < rozmer; x++) {
                ImageIcon horizontalIcon = new ImageIcon("assets/horizontal_line.png");
                g.drawImage(horizontalIcon.getImage(), x * velkostPolicka, y * velkostPolicka - velkostPolicka / 2, this);
            }
        }
        for (int y = 0; y < rozmer; y++) {
            for (int x = 0; x < rozmer; x++) {
                ImageIcon imageIcon;
                if (piskvorky[y][x] == 1) {
                    imageIcon = new ImageIcon("assets/cross.png");
                } else if (piskvorky[y][x] == 2) {
                    imageIcon = new ImageIcon("assets/circle.png");
                } else {
                    continue;
                }
                g.drawImage(imageIcon.getImage(), x * velkostPolicka, y * velkostPolicka, this);
            }
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int riadok = (e.getY() - 40) / velkostPolicka;
        int stlpec = (e.getX() - 10) / velkostPolicka;
        if (stlpec >= rozmer || riadok >= rozmer) {
            return;
        }
        if (vyhralHrac == 0 && tah < rozmer * rozmer) {
            if (piskvorky[riadok][stlpec] == 0) {
                if (writeToDatabase(stlpec, riadok)) {
                    lastMove = me;
                }
            }
        }
    }

    private void checkIfEndGame() {
        if (vyhralHrac > 0) {
            String text = String.format("Vyhral hrac %d (%s).", vyhralHrac, hraci[vyhralHrac - 1]);
            JOptionPane.showMessageDialog(null, text);
            koniecHry = true;
        } else if (tah == rozmer * rozmer) {
            JOptionPane.showMessageDialog(null, "Hra skoncila remizou.");
            koniecHry = true;
        }
        if (koniecHry) {
            vyhralHrac = 0;
            tah = 0;
            lastMove = 0;
            init();
            repaint();
            koniecHry = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private void check() {
        // kontrola riadkov, stlpcov a diagonal
        for (int riadokStlpecDiagonala = 0; riadokStlpecDiagonala < 4; riadokStlpecDiagonala++) {
            for (int y = 0; y < rozmer; y++) {
                for (int hrac = 0; hrac < hraci.length; hrac++) {
                    if (vyhralHrac == 0) {
                        for (int x = 0; x < rozmer; x++) {
                            int pomX = x;
                            int pomY = y;
                            if (riadokStlpecDiagonala == 1) {
                                pomY = x;
                                pomX = y;
                            } else if (riadokStlpecDiagonala == 2) {
                                pomY = x;
                            } else if (riadokStlpecDiagonala == 3) {
                                pomY = x;
                                pomX = rozmer - 1 - x;
                            }
                            if (piskvorky[pomY][pomX] != hrac + 1) {
                                vyhralHrac = 0;
                                break;
                            }
                            vyhralHrac = hrac + 1;
                        }
                    }
                }
                if (riadokStlpecDiagonala > 1) {
                    break;
                }
            }
        }
        checkIfEndGame();
    }

    private boolean writeToDatabase(int x, int y) {
        if (gameId == null || lastMove == me) {
            return false;
        }
        Map<String, Object> move = new HashMap<>();
        move.put("x", x);
        move.put("y", y);

        Map<String, Object> data = new HashMap<>();
        data.put("move", move);
        data.put("player", me);
        data.put("date", FieldValue.serverTimestamp());
        return db.collection("games").document(gameId).collection("moves").add(data).isDone();
    }


    public String createGame() {
        Map<String, Object> data = new HashMap<>();
        data.put("created", FieldValue.serverTimestamp());

        ApiFuture<DocumentReference> result = db.collection("games").add(data);
        try {
            gameId = result.get().getId();
            me = 1;
            return gameId;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean gameExist(String gameId) {
        if (gameId == null) {
            return false;
        }
        ApiFuture<DocumentSnapshot> result = db.collection("games").document(gameId).get();
        try {
            if (result.get().exists()) {
                me = 2;
                this.gameId = gameId;
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void listenToDatabase() {
        EventListener<QuerySnapshot> eventListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirestoreException e) {
                Map<String, Object> data = queryDocumentSnapshots.getDocuments().get(0).getData();
                int player = Math.toIntExact((Long) data.get("player"));
                Map<String, Long> move = (Map<String, Long>) data.get("move");
                int y = Math.toIntExact(move.get("y"));
                int x = Math.toIntExact(move.get("x"));
                piskvorky[y][x] = player;
                tah++;
                lastMove = Math.toIntExact(player);
                repaint();
                check();
            }
        };
        db.collection("games").document(gameId).
                collection("moves").orderBy("date", Query.Direction.DESCENDING).limit(1).addSnapshotListener(eventListener);
    }

    public String getGameId() {
        return gameId;
    }

}
