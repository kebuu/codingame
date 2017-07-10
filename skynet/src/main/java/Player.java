import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {

        Jeu jeu = new Jeu();

        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // nombre total de noeud dans le niveau, y compris les passerelles
        System.err.println("Nombre de noeuds: " + N);
        int L = in.nextInt(); // le nombre de lien
        System.err.println("Nombre de liens: " + L);
        int E = in.nextInt(); // le nombre de passerelle
        System.err.println("Nombre de passerelles: " + E);

        for (int i = 0; i < L; i++) {
            int N1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int N2 = in.nextInt();

            Lien lien = new Lien(N1, N2);
            jeu.liens.add(lien);
            System.err.println("Nouveau lien : " + lien);
        }

        for (int i = 0; i < E; i++) {
            int EI = in.nextInt(); // the index of a gateway node
            System.err.println("Passerelle : " + EI);
            jeu.passerelles.add(new Noeud(EI));
        }

        // game loop
        while (true) {
            int SI = in.nextInt(); // Noeud sur lequel se deplace le virus

            jeu.noeudVirus = new Noeud(SI);

            // Example: 0 1 are the indices of the nodes you wish to sever the link between
            Lien lienACouper = jeu.trouveLeLienACouper();

            if (lienACouper != null) {
                lienACouper.couper();
            }
        }
    }

    static class Jeu {
        List<Lien> liens = new ArrayList<>();
        List<Noeud> passerelles = new ArrayList<>();
        Noeud noeudVirus;

        public Lien trouveLeLienACouper() {

            Lien lienACouper = liens.stream()
                    .filter(lien -> lien.ouvert)
                    .filter(lien -> lien.contient(noeudVirus))
                    .filter(lien -> passerelles.stream().anyMatch(lien::contient))
                    .findFirst()
                    .orElse(liens.stream()
                            .filter(lien -> lien.ouvert)
                            .filter(lien -> passerelles.stream().anyMatch(lien::contient))
                            .findFirst()
                            .get()
                    );

            return lienACouper;
        }
    }

    static class Lien {
        final Noeud noeud_1;
        final Noeud noeud_2;
        boolean ouvert = true;

        Lien(Noeud noeud_1, Noeud noeud_2) {
            this.noeud_1 = noeud_1;
            this.noeud_2 = noeud_2;
        }

        boolean contient(Noeud noeud) {
            return noeud_1.equals(noeud) || noeud_2.equals(noeud);
        }

        Lien(int noeud_1, int noeud_2) {
            this(new Noeud(noeud_1), new Noeud(noeud_2));
        }

        @Override
        public boolean equals(Object o) {
            Lien lien = (Lien) o;

            return noeud_2.equals(lien.noeud_2) && noeud_1.equals(lien.noeud_1) ||
                noeud_2.equals(lien.noeud_1) && noeud_1.equals(lien.noeud_2);
        }

        @Override
        public int hashCode() {
            return noeud_1.hashCode() + noeud_2.hashCode();
        }

        @Override
        public String toString() {
            return "Lien " + noeud_1 + "-" + noeud_2;
        }

        public void couper() {
            String info = noeud_1.index + " " + noeud_2.index;
            ouvert = false;
            System.err.println(info);
            System.out.println(info);
        }
    }

    static class Noeud {
        final int index;

        Noeud(int index) {
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Noeud noeud = (Noeud) o;

            return index == noeud.index;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public String toString() {
            return "Node " + index;
        }
    }
}