package seedu.address.logic.parser.accounting;

import seedu.address.logic.commands.accounting.ListDebtRequestReceivedCommand;
import seedu.address.logic.parser.Parser;
import seedu.address.logic.parser.exceptions.ParseException;

public class ListDebtRequestReceivedCommandParser implements Parser<ListDebtRequestReceivedCommand> {
    @Override
    public ListDebtRequestReceivedCommand parse(String userInput) throws ParseException {
        return new ListDebtRequestReceivedCommand();
    }
}
