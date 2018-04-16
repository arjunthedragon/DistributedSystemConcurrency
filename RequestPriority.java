public enum RequestPriority {
	RequestPriorityVeryLow,
	RequestPriorityLow,
	RequestPriorityMedium,
	RequestPriorityHigh,
	RequestPriorityVeryHigh;

	public static int priorityValue(RequestPriority requestPriority) {
		int priorityValue = 1;

		switch(requestPriority) {
			case RequestPriorityVeryLow : priorityValue = 1;
														 				break;

			case RequestPriorityLow : 		priorityValue = 2;
														 				break;

			case RequestPriorityMedium : 	priorityValue = 3;
														 				break;

			case RequestPriorityHigh : 		priorityValue = 4;
														 				break;
			
			case RequestPriorityVeryHigh : priorityValue = 5;
																		break;

			default: 							 				break;
		}

		return priorityValue;
	}

	public static RequestPriority requestPriority(int priorityValue) {
		RequestPriority requestPriority= RequestPriorityVeryLow;

		switch(priorityValue) {
		  case 1	: requestPriority = RequestPriorityVeryLow;
														 			break;
      case 2	: requestPriority = RequestPriorityLow;
														 			break;

			case 3	: requestPriority = RequestPriorityMedium;
														 			break;

			case 4	: requestPriority = RequestPriorityHigh;
														 			break;

			case 5	: requestPriority = RequestPriorityVeryHigh;
														 			break;

			default : 							 		break;
		}

		return requestPriority;
	}

};


