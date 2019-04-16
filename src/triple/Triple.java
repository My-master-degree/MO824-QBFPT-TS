package triple;

/**
 * An object of this class represents a prohibited triple for MAXQBFPT problem.
 * 
 * @author Jônatas Trabuco Belotti [jonatas.t.belotti@hotmail.com]
 * @author Felipe de Carvalho Pereira [felipe.pereira@students.ic.unicamp.br]
 */
public class Triple {

    public final TripleElement[] elements;

    public Triple(TripleElement te1, TripleElement te2, TripleElement te3) {
        this.elements = new TripleElement[3];
        elements[0] = te1;
        elements[1] = te2;
        elements[2] = te3;
        
    }

    public TripleElement[] getElements() {
        return elements;
    }

    public void printTriple() {
        System.out.print("[" + elements[0].getIndex() + ", ");
        System.out.print(elements[1].getIndex() + ", ");
        System.out.println(elements[2].getIndex() + "]");
    }
}
