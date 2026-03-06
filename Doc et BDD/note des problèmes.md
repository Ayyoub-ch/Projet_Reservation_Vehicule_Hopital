06/03

Choses faites: 
-changer le mdp
-réussir à se connecter

Remarques sur le projet:
-Quand on fait le choix 1, on a aucune saisie possible uniquement du texte 
(voir menuReservation qui est vide) 
LA FONCTION EST VIDE

-Quand on fait le choix 2, on a la possibilité de marquer les caractéristiques du véhicule
sauf qu'après cela nous met la position (ex 8) mais il y a une erreur de type 
via la colonne notype qui ne semble pas exister
===== VERIFICATION DE DISPONIBILITE =====
Entrez la marque du véhicule : Peugeot 
Entrez le modèle du véhicule : Partner 
Entrez le numéro du type : 4       
Entrez l'immatriculation du véhicule : CD456EF 
ERREUR récupération Type : ERREUR: la colonne « notype » n'existe pas
  Position : 8


-Quand on fait le choix 3, c'est quasiment la même chose que pour le 2, aucune réservation n'est trouvée malgré les bonnes saisies et la colonne nodemande n'existe pas (ce qui est vrai car n'étant pas dans la bdd)


Petite pensée à faire : retirer la commentation des clearConsole() dans Menu.java