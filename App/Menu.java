import java.time.LocalDate;
import java.util.Scanner;

public class Menu {

    public void afficherBienvenue() {
        System.out.println("===== BIENVENUE SUR LE SYSTEME DE RESERVATION DU CENTRE HOSPITALIER =====");
    }

    public void afficherMenu(Scanner scanner, Passerelle db) {
        boolean continuer = true;
        String role = db.getRoleConnecte();
        boolean isAdmin = "Admin".equals(role);

        while (continuer) {
            clearConsole();
            afficherBienvenue();

            if (isAdmin) {
                System.out.println("\n===== MENU ADMINISTRATEUR =====");
            } else {
                System.out.println("\n===== MENU PRINCIPAL =====");
            }

            System.out.println("1. Faire une réservation");
            System.out.println("2. Vérifier la disponibilité d'un véhicule");
            System.out.println("3. Modifier une réservation");
            System.out.println("4. Voir mes réservations");

            if (isAdmin) {
                System.out.println("5. Voir toutes les réservations [ADMIN]");
                System.out.println("6. Valider / Refuser des réservations [ADMIN]");
            }

            System.out.println("0. Quitter");

            System.out.print("Veuillez choisir une option : ");

            int choix;
            try {
                choix = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Option invalide. Veuillez réessayer.");
                continue;
            }

            switch (choix) {
                case 1:
                    menuReservation(scanner, db, db.getMatriculeConnecte());
                    break;
                case 2:
                    menuVerificationDisponibilite(scanner, db);
                    break;
                case 3:
                    menuModification(scanner, db);
                    break;
                case 4:
                    clearConsole();
                    db.afficherMesReservations("" + db.getMatriculeConnecte());
                    System.out.println("\nAppuyez sur Entrée pour continuer...");
                    scanner.nextLine();
                    break;
                case 5:
                    if (isAdmin) {
                        clearConsole();
                        db.afficherToutesLesReservations();
                        System.out.println("\nAppuyez sur Entrée pour continuer...");
                        scanner.nextLine();
                    } else {
                        System.out.println("❌ Accès refusé - droits insuffisants.");
                    }
                    break;
                case 6:
                    if (isAdmin) {
                        menuValidationReservation(scanner, db);
                    } else {
                        System.out.println("❌ Accès refusé - droits insuffisants.");
                    }
                    break;
                case 0:
                    System.out.println("Au revoir !");
                    continuer = false;
                    break;
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
                    break;
            }
        }
    }

    public boolean menuConnexion(Scanner s, Passerelle db) {
        clearConsole();
        afficherBienvenue();

        System.out.println("===== CONNEXION =====");
        System.out.println("(Tapez 'quit' pour quitter)\n");

        boolean connecte = false;
        int tentatives = 0;
        final int MAX_TENTATIVES = 3;

        while (!connecte && tentatives < MAX_TENTATIVES) {
            System.out.print("Entrez votre matricule : ");
            String matriculeStr = s.nextLine().trim();

            if (matriculeStr.equalsIgnoreCase("quit")) {
                return false;
            }

            int matricule;
            try {
                matricule = Integer.parseInt(matriculeStr);
            } catch (NumberFormatException e) {
                System.out.println("❌ Le matricule doit être un nombre entier.");
                tentatives++;
                continue;
            }

            System.out.print("Entrez votre mot de passe : ");
            String mdp = s.nextLine();

            connecte = db.verifierConnexion(matricule, mdp);

            if (!connecte) {
                tentatives++;
                if (tentatives < MAX_TENTATIVES) {
                    System.out.println(
                            "Veuillez réessayer. (" + (MAX_TENTATIVES - tentatives) + " tentatives restantes)");
                    System.out.println("\nAppuyez sur Entree pour continuer...");
                    s.nextLine();
                    clearConsole();
                    afficherBienvenue();
                    System.out.println("===== CONNEXION =====");
                    System.out.println("(Tapez 'quit' pour quitter)\n");
                } else {
                    System.out.println("\nNombre maximum de tentatives atteint.");
                }
            }
        }

        return connecte;
    }

    public void menuReservation(Scanner s, Passerelle db, int matriculeConnecte) {
        clearConsole();
        System.out.println("\n===== NOUVELLE RESERVATION =====");

        try {
            // Date de réservation (aujourd'hui)
            System.out.print("Date de réservation (AAAA-MM-JJ) [Entrée = aujourd'hui] : ");
            String dateReservStr = s.nextLine();
            String dateReserv = dateReservStr.isBlank() ? LocalDate.now().toString() : dateReservStr;

            // Date de début
            System.out.print("Date de début d'utilisation (AAAA-MM-JJ) : ");
            String dateDebut = s.nextLine();

            // Durée
            System.out.print("Durée de la réservation (en jours) : ");
            int duree = s.nextInt();
            s.nextLine();

            // Date de retour effectif
            System.out.print("Date de retour effectif (AAAA-MM-JJ ou vide) : ");
            String dateRetourStr = s.nextLine();
            String dateRetourEffectif = dateRetourStr.isBlank() ? null : dateRetourStr;

            
            // Afficher les types disponibles
            System.out.println("\n--- Types de véhicules disponibles ---");
            db.afficherTypes();
            System.out.print("\nNuméro du type de véhicule : ");
            int noType = s.nextInt();
            s.nextLine();

            // Afficher les véhicules disponibles de ce type
            System.out.println("\n--- Véhicules disponibles ---");
            db.afficherVehiculesParType(noType);
            System.out.print("\nImmatriculation du véhicule : ");
            String immat = s.nextLine();

            // Récupérer la personne connectée
            Personne personne = db.recupererPersonneParMatricule(matriculeConnecte);
            if (personne == null) {
                System.out.println("\nERREUR - Impossible de récupérer vos informations.");
                return;
            }

            // Récupérer le véhicule
            Vehicule vehicule = db.recupererVehiculeParImmat(immat);
            if (vehicule == null) {
                System.out.println("\nERREUR - Véhicule introuvable.");
                return;
            }

            // Créer la demande
            Demande nouvelleDemande = new Demande(
                    dateReserv,
                    dateDebut,
                    personne,
                    noType,
                    vehicule,
                    duree,
                    dateRetourEffectif, // date de retour effectif
                    "En attente");

            // Enregistrer la demande
            if (nouvelleDemande.faireDemande(db)) {
                System.out.println("\n✅ Réservation créée avec succès !");
                System.out.println("Véhicule : " + vehicule.getMarque() + " " + vehicule.getModele());
                System.out.println("Du " + dateDebut + " pour " + duree + " jour(s)");
            } else {
                System.out.println("\n❌ Erreur lors de la création de la réservation.");
            }

        } catch (Exception e) {
            System.out.println("\n❌ ERREUR - " + e.getMessage());
        }

        System.out.println("\nAppuyez sur Entrée pour revenir au menu principal...");
        s.nextLine();
    }

    public void menuVerificationDisponibilite(Scanner s, Passerelle db) {
        clearConsole();
        System.out.println("===== VERIFICATION DE DISPONIBILITE =====\n");

        // Demande des informations au utilisateur
        System.out.print("Entrez la marque du véhicule : ");
        String marque = s.nextLine();

        System.out.print("Entrez le modèle du véhicule : ");
        String modele = s.nextLine();

        System.out.print("Entrez le numéro du type : ");
        int numType = s.nextInt();
        s.nextLine(); // consomme le retour à la ligne

        System.out.print("Entrez l'immatriculation du véhicule : ");
        String immat = s.nextLine();

        // Création de l'objet Type à partir du numéro saisi
        Type unType = db.recupererTypeParNumero(numType);

        if (unType == null) {
            System.out.println("\nERREUR : le type de véhicule saisi n'existe pas.");
            System.out.println("Appuyez sur Entrée pour revenir au menu principal...");
            s.nextLine();
            return; // quitte la méthode
        }

        // Appel de la fonction de vérification de réservation
        boolean existe = db.verifierReservation(marque, modele, unType, immat);

        // Si le véhicule est dispo
        if (!existe) {
            System.out.println("Ce vehicule " + modele + " est disponible.");

            System.out.println("\nVotre reservation a ete validee pour les vehicules suivants :");
            // TO STRING des véhicules les affichant en réservation
            // Avec le nom, le prénom et les véhicules pour telle heure
        } else {
            System.out.println("Desole ce vehicule " + modele + " n est pas disponible.");
            // Autrement

            // Refaire la demande au besoin
            System.out.println("Aucun vehicule disponible. Merci de reessayer plus tard.");
        }

        System.out.println("\nVerification terminee !");
        System.out.println("\nAppuyez sur Entree pour revenir au menu principal...");
        s.nextLine();
    }

    public void menuModification(Scanner s, Passerelle db) {
        clearConsole();
        System.out.println("\n===== MODIFIER UNE RESERVATION =====");

        try {
            System.out.print("Numéro de la réservation : ");
            int numero = s.nextInt();
            s.nextLine();

            System.out.print("Date de la réservation (AAAA-MM-JJ) : ");
            LocalDate datereserv = LocalDate.parse(s.nextLine());

            if (!db.reservationExiste(numero, datereserv)) {
                System.out.println("\n❌ Aucune réservation trouvée avec ce numéro et cette date !");
            } else {
                System.out.println("\n✅ Réservation trouvée. Saisissez les champs à modifier :");
                System.out.print("Nouvelle date de début (AAAA-MM-JJ) : ");
                LocalDate dateDebut = LocalDate.parse(s.nextLine());

                System.out.print("Matricule du personnel : ");
                int matricule = s.nextInt();
                s.nextLine();

                System.out.print("Numéro du type : ");
                int noType = s.nextInt();
                s.nextLine();

                System.out.print("Immatriculation du véhicule : ");
                String immat = s.nextLine();

                System.out.print("Durée (jours) : ");
                int duree = s.nextInt();
                s.nextLine();

                System.out.print("Date retour effectif (AAAA-MM-JJ ou vide) : ");
                String dateRetourStr = s.nextLine();
                LocalDate dateRetourEffectif = dateRetourStr.isBlank() ? null : LocalDate.parse(dateRetourStr);

                System.out.print("État de la réservation : ");
                String etat = s.nextLine();

                // Appel à la passerelle
                db.modifierReservation(numero, datereserv, dateDebut, matricule, noType, immat, duree,
                        dateRetourEffectif, etat);
            }
        } catch (Exception e) {
            System.out.println("\n❌ ERREUR - " + e.getMessage());
        }

        System.out.println("\nAppuyez sur Entrée pour revenir au menu principal...");
        s.nextLine();
    }

    public void menuValidationReservation(Scanner s, Passerelle db) {
        clearConsole();
        System.out.println("\n===== VALIDATION DES RESERVATIONS [ADMIN] =====");

        db.afficherReservationsEnAttente();

        System.out.println("\nEntrez le numéro de la réservation à traiter (0 pour annuler) : ");
        int numero;
        try {
            numero = Integer.parseInt(s.nextLine().trim());
        } catch (NumberFormatException ignored) {
            System.out.println("❌ Numéro invalide.");
            System.out.println("\nAppuyez sur Entrée pour revenir au menu principal...");
            s.nextLine();
            return;
        }

        if (numero == 0) {
            return;
        }

        System.out.print("Date de la réservation (AAAA-MM-JJ) : ");
        LocalDate datereserv;
        try {
            datereserv = LocalDate.parse(s.nextLine().trim());
        } catch (Exception ignored) {
            System.out.println("❌ Date invalide.");
            System.out.println("\nAppuyez sur Entrée pour revenir au menu principal...");
            s.nextLine();
            return;
        }

        System.out.println("\nQue souhaitez-vous faire ?");
        System.out.println("  1. Valider la réservation");
        System.out.println("  2. Refuser la réservation");
        System.out.println("  0. Annuler");
        System.out.print("Votre choix : ");

        int choix;
        try {
            choix = Integer.parseInt(s.nextLine().trim());
        } catch (NumberFormatException ignored) {
            System.out.println("❌ Choix invalide.");
            System.out.println("\nAppuyez sur Entrée pour revenir au menu principal...");
            s.nextLine();
            return;
        }

        switch (choix) {
            case 1:
                db.validerReservation(numero, datereserv);
                break;
            case 2:
                db.refuserReservation(numero, datereserv);
                break;
            case 0:
                System.out.println("Opération annulée.");
                break;
            default:
                System.out.println("❌ Choix invalide.");
                break;
        }

        System.out.println("\nAppuyez sur Entrée pour revenir au menu principal...");
        s.nextLine();
    }

    public void menuResumeFinal() {
        // Message Final
        System.out.println("\n===== RESUME FINAL =====");
        System.out.println("Vehicules reserves :");
        // Affichage du nombre de Véhicules via To STRING

        System.out.println("\nMerci d avoir utilise notre service de reservation !");
        System.out.println(" Au revoir !");
    }

    public static void clearConsole() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

}
