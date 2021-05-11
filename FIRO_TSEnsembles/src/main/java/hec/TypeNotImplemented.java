package hec;

public class TypeNotImplemented extends RuntimeException{

    /**
     *
     */
    private static final long serialVersionUID = -1647485174863734061L;

    public TypeNotImplemented(String type_name){
        super(type_name);
    }

}
