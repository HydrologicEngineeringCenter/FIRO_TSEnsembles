package hec.exceptions;

public class InvalidMember extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InvalidMember(String member_name) {        
        super(member_name + " is not validly named for this collection");
    }
}