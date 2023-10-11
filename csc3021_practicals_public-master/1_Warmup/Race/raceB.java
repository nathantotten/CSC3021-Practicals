/*----------------------------------------------------
  This program illustrates another example of a race
  condition.

  Two threads attemps to add a different element to the end
  of a list at approximately the same time.

  Study the program. What are the four possible outputs ?
  Run the program several times and try and produce all
  four outputs.
  -------------------------------------------------------*/
import java.util.Random;

class raceB {
  public static list l = new list();

  public static void main (String[] args) {
    for (int i=0; i<5; i++)
      l.add(i);
    Thread t1 = new Thread(new A(l,99));
    Thread t2 = new Thread(new A(l,66));
    t1.start();
    t2.start();
    Time.delay(200);
    System.out.println("list length = "+l.listLength());
    l.print();
  }
}

class list {
  private listElement head = null;
  private listElement tail = null;

  public void add(int e) {
    listElement newElement = new listElement();
    newElement.data = e;
    if (head==null) {
      head=newElement;
      tail=newElement;
    }
    else {
      Time.delay(A.rnd.nextInt(20));
      tail.next = newElement;
      Time.delay(A.rnd.nextInt(20));
      tail = newElement;
    }
  }

  public void print() {
    listElement current = head;
    while(current!=null) {
      System.out.println(current.data);
      current=current.next;
    }
  }

  public int listLength() {
    listElement current = head;
    int length=0;
    while(current!=null) {
      length++;
      current=current.next;
    }
    return length;
  }
}

class listElement {
  public int data = 0;
  public listElement next = null;
}

class A implements Runnable {  
  private list l;
  private int data;
  static public Random rnd = new Random();

  public A(list l, int data) {
    this.l =l; this.data = data;
  }
  public void run() {
    l.add(data);
    System.out.println(data+": finished");
  }
}
