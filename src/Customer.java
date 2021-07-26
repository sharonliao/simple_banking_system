import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Customer implements Runnable{
    String name;
    int credit;
    int remaidCredit;
    ArrayList<Bank> bankList;
    Lock lock = new ReentrantLock();
    Condition newReply = lock.newCondition();
    public String reply = "";
    public int requestAmount;


    Customer(String name,int credit,ArrayList<Bank> bankList){
        this.name = name;
        this.credit = credit;
        this.remaidCredit = credit;
        this.bankList = (ArrayList<Bank>)bankList.clone();
    }

    public String toString(){
        return name + ":" + remaidCredit;
    }


    /**
     * handle the approval reply
     * @param approvalAmount
     * @param bankName
     */
    public void reduceCredit(int approvalAmount,String bankName){
        remaidCredit -= approvalAmount;
        System.out.println(bankName + " approves a loan of "+approvalAmount+" dollars from "+name);
        try{
            Thread.sleep(100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * this function sends requests to banks from its bank list,
     * it will not send the next request until getting a result from the previous bank.
     * @param banksList
     */
    public void getLoan(ArrayList<Bank> banksList){
        Random rand = new Random();
        while (remaidCredit>0 && banksList.size()>0){
            int randomIndex = rand.nextInt(banksList.size());
            Bank bank = banksList.get(randomIndex);

            requestAmount = rand.nextInt(49)+1;
            if(requestAmount>remaidCredit){
                requestAmount = remaidCredit;
            }
            System.out.println(name+" requests a loan of "+requestAmount+" dollar(s) from "+ bank.name);
            bank.acceptRequest(this);


            bank.lock.lock();
            try {
                bank.newRequest.signalAll();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                bank.lock.unlock();
            }

            lock.lock();
            try{
                while (reply.length() == 0){
                    newReply.await();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }


            if(reply.length()>0){
                if(reply.equals("Approve")){
                    reduceCredit(requestAmount,bank.name);
                }else {
                    banksList.remove(bank);
                    System.out.println(bank.name+" denies a loan of "+ requestAmount +" dollars from "+name );
                }
            }
            reply = "";
            try{
                Thread.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(remaidCredit == 0){
            System.out.println(name+" has reached the objective of "+credit+" dollar(s). Woo Hoo!");

        }else if(remaidCredit > 0){
            System.out.println(name+" was only able to borrow "+(credit-remaidCredit)+" dollar(s). Woo Hoo!");
        }
    }



    public synchronized void getReply(String replyStr){
        reply = replyStr;
    }

    /**
     * the customer thread will run the function getLoan()
     */
    public void run(){
        try{
            getLoan(bankList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}