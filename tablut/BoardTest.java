package tablut;

public class BoardTest {

    public static void main(String[] args) {
        Board b = new Board();
        System.out.println(b);
        Square fro = Square.sq(0, 4);
        Square to = Square.sq(0, 0);
        b.makeMove(fro, to);
        System.out.println(b);
    }
}
