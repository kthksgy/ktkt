package ktkt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import ktkt.dao.Attendances;
import ktkt.dto.Attendance;
import ktkt.dto.Employee;

public class Summary {
  private Employee owner;
  private List<Attendance> as = new LinkedList<Attendance>();
  private static final String WORKING_MARK = "+";
  private static final String NOT_WORKING_MARK = " ";

  public Summary(Employee owner, LocalDate begin, LocalDate end) {
    this(owner, Attendances.getAttendances(owner.getId(), begin, end));
  }

  public Summary(Employee owner, int num) {
    this(owner, Attendances.getAttendances(owner.getId(), num));
  }

  public Summary(Employee owner, List<Attendance> as) {
    this.owner = owner;
    this.as = as;
    if (this.as != null) {
      this.as.sort(null);
    }
  }

  public Attendance get(int index) {
    try {
      return as.get(index - 1);
    } catch (NullPointerException e) {
      return null;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public int size() {
    return as.size();
  }

  public boolean currentState() {
    if (as.isEmpty()) {
      return false;
    } else {
      return as.get(as.size() - 1).getState();
    }
  }

  public void add(Attendance a) {
    as.add(a);
  }

  public boolean validate(Attendance a) {
    return validate(a.getTime(), a.getState());
  }

  public boolean validate(LocalDateTime ldt, boolean state) {
    return validate(ldt.toLocalTime(), state);
  }

  public boolean validate(LocalTime lt, boolean state) {
    return validate(lt.toSecondOfDay(), state);
  }

  public boolean validate(int t, boolean state) {
    LongSummaryStatistics stats = as.stream().filter(a -> a.getState() == state)
        .map(a -> a.getTime().toLocalTime().toSecondOfDay()).collect(LongSummaryStatistics::new,
            LongSummaryStatistics::accept, LongSummaryStatistics::combine);
    return Math.abs(t - (int) stats.getAverage()) < 1200;
  }

  public void print() {
    owner.print();
    for (int i = 0; i < as.size(); i++) {
      System.out.printf("%3d. ", i + 1);
      as.get(i).print();
    }
  }

  public void visualize() {
    owner.print();
    if (!as.isEmpty()) {
      System.out.print("            | 0     6    12    18    24");
      boolean isWorked = false;
      LocalDateTime p = as.get(0).getTime().truncatedTo(ChronoUnit.DAYS);
      LocalDate tmp = p.toLocalDate().minusDays(1);
      int i = 0;
      while (p.compareTo(as.get(as.size() - 1).getTime()) <= 0) {
        if (tmp.compareTo(p.toLocalDate()) < 0) {
          System.out.printf("\n%11s | ", p.toLocalDate());
        }
        if (i < as.size()) {
          while (i < as.size() && p.compareTo(as.get(i).getTime()) >= 0) {
            i++;
            if (i < as.size() && !as.get(i).getState()) {
              isWorked = true;
            }
          }
          if (i < as.size() && !as.get(i).getState()) {
            isWorked = true;
          }
        } else {
          if (as.get(as.size() - 1).getState()) {
            System.out.println("i >= as.size()");
            isWorked = true;
          }
        }
        System.out.print(isWorked ? WORKING_MARK : NOT_WORKING_MARK);
        tmp = p.toLocalDate();
        p = p.plusHours(1);
        isWorked = false;
      }
      System.out.println();
    } else {
      System.out.println("--- 勤怠情報無し ---");
    }
  }
}
