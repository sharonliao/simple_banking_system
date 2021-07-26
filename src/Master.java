import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master {
    public static void main(String[] args){
        Master master = new Master();

        String BankFilePath = "src/banks.txt";
        String CustomersFilePath = "src/customers.txt";
        ArrayList<String> banks = master.input( BankFilePath);
        ArrayList<String> customers = master.input(CustomersFilePath);

        ArrayList<Bank> banksList = master.startAllBanks(banks);
        ArrayList<Customer> customersList = master.startAllCustomers(customers,banksList);

        System.out.println("\n** Customers and loan objectives **");
        for(Customer customer : customersList){
            System.out.println(customer);
        }
        System.out.println("\n\n** Banks and financial resources **");
        for(Bank bank : banksList){
            System.out.println(bank);
        }
        System.out.println("\n");


        ExecutorService executor = Executors.newCachedThreadPool();

        //start all banks thread
        for(Bank bank : banksList){
            executor.execute(bank);
        }

        //start all customers thread
        for(Customer customer : customersList){
            executor.execute(customer);
        }


        while (!master.isEnd(customersList)){
            continue;
        }
        try{
            for(Bank bank : banksList){
                bank.isEnd = true;
            }
            executor.shutdown();
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("\n\n======================== RESULT ======================");
        for(Bank bank : banksList){
            System.out.println(bank.name+" has "+bank.remainResouce+" dollar(s) remaining");
        }
        for(Customer customer : customersList){
            if(customer.remaidCredit == 0){
                System.out.println(customer.name+" has reached the objective of "+customer.credit+" dollar(s). Woo Hoo!");
            }else if(customer.remaidCredit > 0){
                System.out.println(customer.name+" was only able to borrow "+(customer.credit-customer.remaidCredit)+" dollar(s). Woo Hoo!");
            }
        }
    }


    public ArrayList<String> input(String nameOfMapFile){
        ArrayList<String> inputArry = new ArrayList<>();
        BufferedReader br = null;
        String inputLine = null;
        try{
            br = new BufferedReader(new FileReader(nameOfMapFile));
            while ((inputLine = br.readLine()) != null) {
                String inStr = inputLine.trim().substring(1,inputLine.trim().length()-2);
                inputArry.add(inStr);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return inputArry;
    }

    public ArrayList<Customer> startAllCustomers(ArrayList<String> customers,ArrayList<Bank> banksList){
        ArrayList<Customer> customerList = new ArrayList<>();
        for(String inStr : customers){
            String[] input = inStr.split(",");
            Customer customer = new Customer(input[0],Integer.parseInt(input[1].trim()),banksList);
            customerList.add(customer);
        }
        return customerList;
    }

    public ArrayList<Bank> startAllBanks(ArrayList<String> banks){
        ArrayList<Bank> BankList = new ArrayList<>();
        for(String inStr : banks){
            String[] input = inStr.split(",");
            Bank bank = new Bank(input[0],Integer.parseInt(input[1].trim()));
            BankList.add(bank);
        }
        return BankList;
    }

    public boolean isEnd(ArrayList<Customer> customersList){
        boolean result = true;
        for(Customer customer : customersList){
            if(customer.bankList.size() != 0 && customer.remaidCredit >0){
                result = false;
            }
        }
        return result;
    }
}




