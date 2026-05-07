import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("   SYSTEME DE RESERVATION DE VEHICULES - CENTRE HOSPITALIER");
        System.out.println("============================================================");
        System.out.println();

        try (java.util.Scanner s = new java.util.Scanner(System.in)) {

            // ── Connexion à la base de données ───────────────────────────────
            Passerelle db = connecterBase(s);

            if (db == null) {
                System.out.println("Au revoir !");
                return;
            }

            // ── Authentification de l'utilisateur ────────────────────────────
            Menu menu = new Menu();
            boolean connecte = menu.menuConnexion(s, db);

            if (!connecte) {
                System.out.println("Connexion annulee.");
            } else {
                menu.afficherMenu(s, db);
            }

            // ── Fermeture ────────────────────────────────────────────────────
            System.out.println("\nMerci d'avoir utilise notre systeme. A bientot !");
            db.closeConnection();
        }
    }

    /**
     * Tente de se connecter à la base de données.
     * Propose à l'utilisateur de réessayer en cas d'échec.
     * @return une Passerelle connectée, ou null si l'utilisateur choisit de quitter
     */
    private static Passerelle connecterBase(java.util.Scanner s) {
        while (true) {
            Menu.clearConsole();
            Passerelle db = new Passerelle();

            if (db.isConnected()) {
                return db;
            }

            System.out.println();
            System.out.println("============================================================");
            System.out.println("              PROBLEME DE CONNEXION A LA BASE");
            System.out.println("============================================================");
            System.out.println("1. Reessayer");
            System.out.println("2. Quitter");
            System.out.print("Votre choix : ");

            String choix = s.nextLine().trim();

            if (!"1".equals(choix)) {
                return null;
            }
        }
    }
}
