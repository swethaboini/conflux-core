package org.mifosplatform.portfolio.client.exception;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class DuedateIsNotMeetingDateException extends AbstractPlatformResourceNotFoundException{
public DuedateIsNotMeetingDateException(LocalDate date){
	super("error.msg.duedate.is.not.meetingDate","duedate is not meetingDate",date);
}
}
