import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Bank implements Runnable{
    String name;
    int resource;
    int remainResouce;
    boolean isEnd = false;
    Lock lock = new ReentrantLock();
    Condition newRequest = lock.newCondition();
    Queue<Customer> request = new LinkedList<>();

    Bank(String name, int resource) {
        this.name = name;
        this.resource = resource;
        this.remainResouce = resource;
    }

    public String toString() {
        return name + ":" + remainResouce;
    }


    /**
     * This function is a bank server which handles the request from all the customers
     */
    public void handAllRequest() {
        while (!isEnd){
            lock.lock();
            try {
                while (request.size() == 0) {
                    newRequest.await();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }

            if (request.size() > 0) {
                Customer customer = pollRequest();
                handleLoan(customer);
            }
        }

    }

    /**
     * Handle the request of loans, reply "Approve" or "Deny" to the customer
     * @param customer
     * @return
     */
    public boolean handleLoan(Customer customer) {
        customer.lock.lock();
        boolean result = false;
        try{
            if (customer.requestAmount <= remainResouce) {
                remainResouce -= customer.requestAmount;
                customer.getReply("Approve");
                result = true;
            } else {
                customer.getReply("Deny");
            }
            customer.newReply.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            customer.lock.unlock();
        }
        return result;
    }

    public synchronized void acceptRequest(Customer customer) {
        request.offer(customer);
    }

    public synchronized Customer pollRequest() {
        return request.poll();
    }

    /**
     * the bank thread will run the function handAllRequest()
     */
    public void run(){
        try{
            handAllRequest();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}