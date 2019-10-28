package seedu.algobase.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.algobase.logic.parser.CliSyntax.PREFIX_PLAN_FROM;
import static seedu.algobase.logic.parser.CliSyntax.PREFIX_PLAN_TO;
import static seedu.algobase.logic.parser.CliSyntax.PREFIX_TASK;
import static seedu.algobase.model.Model.PREDICATE_SHOW_ALL_PLANS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.algobase.commons.core.Messages;
import seedu.algobase.commons.core.index.Index;
import seedu.algobase.logic.commands.exceptions.CommandException;
import seedu.algobase.model.Model;
import seedu.algobase.model.plan.Plan;
import seedu.algobase.model.task.Task;

/**
 * Moves a Task from one Plan to another.
 */
public class MoveTaskCommand extends Command {

    public static final String COMMAND_WORD = "movetask";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Moves the Task identified by the index from one plan to another.\n"
            + "Parameters:\n"
            + PREFIX_TASK + "TASK_INDEX "
            + PREFIX_PLAN_FROM + "PLAN_INDEX "
            + PREFIX_PLAN_TO + "PLAN_INDEX\n"
            + "Example:\n"
            + COMMAND_WORD + " "
            + PREFIX_TASK + "10 "
            + PREFIX_PLAN_FROM + "1 "
            + PREFIX_PLAN_TO + "2";

    public static final String MESSAGE_MOVE_TASK_SUCCESS = "Task [%1$s] moved from Plan [%2$s] to Plan [%3$s].";
    public static final String MESSAGE_DUPLICATE_TASK = "Task [%1$s] already exists in Plan [%2$s].";

    private final MoveTaskDescriptor moveTaskDescriptor;

    /**
     * Creates a MoveTaskCommand to move a {@code Task} from the specified {@code Plan} to another
     *
     * @param moveTaskDescriptor details of the plan and problem involved
     */
    public MoveTaskCommand(MoveTaskDescriptor moveTaskDescriptor) {
        this.moveTaskDescriptor = moveTaskDescriptor;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Plan> lastShownPlanList = model.getFilteredPlanList();

        if (moveTaskDescriptor.planFromIndex.getZeroBased() >= lastShownPlanList.size()
            || moveTaskDescriptor.planToIndex.getZeroBased() >= lastShownPlanList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        Plan planFrom = lastShownPlanList.get(moveTaskDescriptor.planFromIndex.getZeroBased());
        List<Task> taskListFrom = new ArrayList<>(planFrom.getTasks());
        Task taskToMove = taskListFrom.get(moveTaskDescriptor.taskIndex.getZeroBased());
        taskListFrom.remove(moveTaskDescriptor.taskIndex.getZeroBased());
        Set<Task> taskSetFrom = new HashSet<>(taskListFrom);
        Plan updatedPlanFrom = Plan.updateTasks(planFrom, taskSetFrom);

        Plan planTo = lastShownPlanList.get(moveTaskDescriptor.planToIndex.getZeroBased());
        List<Task> taskListTo = new ArrayList<>(planTo.getTasks());
        Set<Task> taskSetTo = new HashSet<>(taskListTo);
        if (taskSetTo.contains(taskToMove)) {
            return new CommandResult(
                String.format(MESSAGE_DUPLICATE_TASK, taskToMove.getProblem().getName(), planTo.getPlanName()));
        }
        taskSetTo.add(taskToMove);
        Plan updatedPlanTo = Plan.updateTasks(planTo, taskSetTo);

        model.setPlan(planFrom, updatedPlanFrom);
        model.setPlan(planTo, updatedPlanTo);
        model.updateFilteredPlanList(PREDICATE_SHOW_ALL_PLANS);
        return new CommandResult(
            String.format(MESSAGE_MOVE_TASK_SUCCESS,
                taskToMove.getProblem().getName(),
                updatedPlanFrom.getPlanName(),
                updatedPlanTo.getPlanName()
            )
        );
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof MoveTaskCommand // instanceof handles nulls
                && moveTaskDescriptor.equals(((MoveTaskCommand) other).moveTaskDescriptor)); // state check
    }

    /**
     * Stores the details of the plan and problem involved.
     */
    public static class MoveTaskDescriptor {
        private Index taskIndex;
        private Index planFromIndex;
        private Index planToIndex;

        public MoveTaskDescriptor(Index taskIndex, Index planFromIndex, Index planToIndex) {
            this.taskIndex = taskIndex;
            this.planFromIndex = planFromIndex;
            this.planToIndex = planToIndex;
        }

        @Override
        public boolean equals(Object other) {
            return other == this // short circuit if same object
                || (other instanceof MoveTaskDescriptor // instanceof handles nulls
                && taskIndex.equals(((MoveTaskDescriptor) other).taskIndex)
                && planFromIndex.equals(((MoveTaskDescriptor) other).planFromIndex)
                && planToIndex.equals(((MoveTaskDescriptor) other).planToIndex));
        }
    }
}