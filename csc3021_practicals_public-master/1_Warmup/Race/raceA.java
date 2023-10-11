/*----------------------------------------------------
  This program illustrates a more suble example of a race
  condition.

  A Bank has 10,000 savings accounts each with £1000.
  The total assets are threfore £10,000,000.
  The automatic teller machine(ATM) continuously picks two
  accounts at random and adds £500 to one and deducts £500
  from the other. The totals assets should therefore remain
  at £10,000,000. Perodically the auditor sums the Bank's assets
  and outputs the total.

  Study the program. Note how the bank account object is
  shared by both threads.
  Run the program several times and observe the output.
  Use <ctrl>C to terminate the program.
  -------------------------------------------------------*/
import java.util.Random;

class raceA {
  public static void main (String[] args) {
    bankAccount ba = new bankAccount();
    Thread atm = new Thread(new ATM (ba)) ;
    Thread auditor = new Thread(new Auditor (ba)) ;

    atm.start () ;
    auditor.start () ;
  }
}

class ATM implements Runnable {
  private bankAccount ba;

  public ATM(bankAccount ba) {
    this.ba = ba;
  }

  public void run ()  {
    Random rnd = new Random();
    while(true) {
      int account1 = rnd.nextInt(ba.numberOfAccounts()-1);
      int account2 = rnd.nextInt(ba.numberOfAccounts()-1);
      ba.withdraw(account1,500);
      ba.deposit(account2,500);
    }
  }
}

class Auditor implements Runnable {
  private bankAccount ba;

  public Auditor(bankAccount ba) {
    this.ba = ba;
  }

  public void run ()  {
    while(true) {
      Time.delay(100);
      ba.totalAssets();
    }
  }
}

class bankAccount {
  private int numberOfAccounts = 10000;
  private int [] savingsAccounts = new int[numberOfAccounts];

  public bankAccount() {
    for (int i=0; i<savingsAccounts.length; i++)
      savingsAccounts[i]=1000;
  }

  public void withdraw(int account, int wd ){
    savingsAccounts[account] = savingsAccounts[account]-wd;
  }

  public void deposit(int account, int d){
    savingsAccounts[account] = savingsAccounts[account]+d;
  }

  public int numberOfAccounts() {
    return savingsAccounts.length;
  }

  public void totalAssets() {
    int total=0;
    for (int i=0; i<savingsAccounts.length; i++)
      total = total +savingsAccounts[i];
    System.out.println("total is " + total);
  }
}
