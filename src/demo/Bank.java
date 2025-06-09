package Bank;

class Account{
    public String name;
    private String password;

    public void printAcc(){
        System.out.println(name);


    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String pass){
        password=pass;
    }

}

public class Bank {
    public static void main(String args[]){
        Account a2 = new Account();
        a2.name=" custmer 1";
        a2.printAcc();
        a2.setPassword("anuj patel");
        a2.getPassword();

    }
    }


