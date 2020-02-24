package hec;

public class UpdateFailure extends RuntimeException{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UpdateFailure(String message, Throwable err){
        super(message,err);
    }
}
