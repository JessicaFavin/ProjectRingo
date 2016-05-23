public class Debug{
  boolean activated;

  public Debug(Boolean a){
    activated = a;
  }

  public void display(String info_message){
    if(activated){
      System.out.println(info_message);
    }
  }

  public void activate(){
    activated = true;
  }

  public void deactivate(){
    activated = false;
  }

  public boolean isActivated(){
    return activated;
  }

}
