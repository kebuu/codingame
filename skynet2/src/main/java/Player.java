import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
@SuppressWarnings("Duplicates")
class Player {

    public static void main(String args[]) {

        Jeu jeu = new Jeu();

        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // nombre total de noeud dans le niveau, y compris les passerelles
        int L = in.nextInt(); // le nombre de lien
        int E = in.nextInt(); // le nombre de passerelle

        for (int i = 0; i < L; i++) {
            int N1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int N2 = in.nextInt();

            Lien lien = new Lien(N1, N2);
            jeu.liens.add(lien);
        }

        for (int i = 0; i < E; i++) {
            int EI = in.nextInt(); // the index of a gateway node
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
                            .sorted(Comparator.comparingInt((Lien lien) -> {
                                Noeud noeudNonPasserelleDuLien = Stream.of(lien.noeud_1, lien.noeud_2).filter(noeud -> !passerelles.contains(noeud)).findFirst().get();
                                return distance(noeudVirus, noeudNonPasserelleDuLien);
                            }).thenComparing((Lien lien) -> {
                                Noeud noeudNonPasserelleDuLien = Stream.of(lien.noeud_1, lien.noeud_2).filter(noeud -> !passerelles.contains(noeud)).findFirst().get();
                                return (int) liens.stream()
                                        .filter(lien1 -> lien1.ouvert)
                                        .filter(lien1 -> lien1.contient(noeudNonPasserelleDuLien) &&
                                                (passerelles.contains(lien1.noeud_1) || passerelles.contains(lien1.noeud_2)))
                                        .count() * -1;
                            }))
                            .findFirst()
                            .get()
                    );

            return lienACouper;
        }

        int distance(Noeud source, Noeud cible) {
            List<AStarNode> openNodes = new ArrayList<>();
            List<AStarNode> closeNodes = new ArrayList<>();

            openNodes.add(new AStarNode(source));

            int distance = -1;

            while (!openNodes.isEmpty()) {
                AStarNode bestStep = openNodes.stream().min(Comparator.comparingInt(AStarNode::getF)).get();

                openNodes.remove(bestStep);

                List<AStarNode> successors = liens.stream()
                        .filter(lien -> lien.contient(bestStep.noeud))
                        .map(lien -> lien.noeud_1.equals(bestStep.noeud) ? lien.noeud_2: lien.noeud_1)
                        .filter(noeud -> !passerelles.contains(noeud))
                        .map(noeud -> {
                            AStarNode aStarNode = new AStarNode(noeud);
                            aStarNode.parent = bestStep;
                            return aStarNode;
                        })
                        .collect(Collectors.toList());

                for (AStarNode successor : successors) {
                    boolean passerelleAccessible = liens.stream()
                            .anyMatch(lien -> lien.ouvert && lien.contient(successor.noeud) && (passerelles.contains(lien.noeud_1) || passerelles.contains(lien.noeud_2)));

                    int stepCost = passerelleAccessible ? 0 : 1;
                    successor.f = bestStep.getF() + stepCost;

                    if (successor.noeud.equals(cible)) {
                        return successor.f;
                    }

                    boolean b = openNodes.stream().anyMatch(aStarNode -> aStarNode.noeud.equals(successor.getNoeud()) && aStarNode.f <= successor.f);
                    boolean c = closeNodes.stream().anyMatch(aStarNode -> aStarNode.noeud.equals(successor.getNoeud()) && aStarNode.f <= successor.f);

                    if (!b && !c) {
                        openNodes.add(successor);
                    }
                }

                closeNodes.add(bestStep);
            }


//            while the open list is not empty
    //            find the node with the least f on the open list, call it "q"
    //            pop q off the open list
    //            generate q's 8 successors and set their parents to q
    //            for each successor
        //            if successor is the goal, stop the search
        //            successor.g = q.g + distance between successor and q
        //            successor.h = distance from goal to successor
        //            successor.f = successor.g + successor.h
        //
        //            if a node with the same position as successor is in the OPEN list \
        //              which has a lower f than successor, skip this successor
        //            if a node with the same position as successor is in the CLOSED list \
        //              which has a lower f than successor, skip this successor
        //            otherwise, add the node to the open list
    //            end
    //            push q on the closed list
//            end

            System.err.println("source:" + source + ",cible:" + cible + ",distance:" + distance);
            return distance;
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

    static class AStarNode {
        AStarNode parent;
        final Noeud noeud;
        int f;
        int g;
        int h;

        AStarNode(Noeud noeud) {
            this.noeud = noeud;
        }

        public AStarNode getParent() {
            return parent;
        }

        public Noeud getNoeud() {
            return noeud;
        }

        public int getF() {
            return f;
        }

        public int getG() {
            return g;
        }

        public int getH() {
            return h;
        }

        @Override
        public String toString() {
            return parent.noeud + "->" + noeud + "-" + f;
        }
    }
}