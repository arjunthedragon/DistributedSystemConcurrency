import java.lang.*;
import java.util.*;

public interface BaseNodeServerInterface {
	public void onReceiveCriticalSectionRequest(boolean isConditionSatisfied, int replyBackPort);
	public void onLeavingCriticalSection(boolean isConditionSatisfied, ArrayList<String> deferredNodes);
}
