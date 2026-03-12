package cn.intentforge.tool.core.gateway;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.model.ToolDefinition;
import cn.intentforge.tool.core.permission.ToolPermissionDecision;
import cn.intentforge.tool.core.permission.ToolPermissionPolicy;
import cn.intentforge.tool.core.registry.ToolRegistration;
import cn.intentforge.tool.core.registry.ToolRegistry;
import cn.intentforge.tool.core.validation.JsonSchemaValidator;
import java.util.List;
import java.util.Objects;

/**
 * Default tool gateway implementation.
 */
public final class DefaultToolGateway implements ToolGateway {
  /**
   * Error code: tool not found.
   */
  public static final String ERROR_TOOL_NOT_FOUND = "TOOL_NOT_FOUND";
  /**
   * Error code: permission denied.
   */
  public static final String ERROR_PERMISSION_DENIED = "PERMISSION_DENIED";
  /**
   * Error code: permission requires confirmation.
   */
  public static final String ERROR_PERMISSION_REQUIRED = "PERMISSION_REQUIRED";
  /**
   * Error code: parameter validation failed.
   */
  public static final String ERROR_INVALID_PARAMETERS = "INVALID_PARAMETERS";
  /**
   * Error code: tool execution failed.
   */
  public static final String ERROR_TOOL_EXECUTION_FAILED = "TOOL_EXECUTION_FAILED";

  private final ToolRegistry toolRegistry;
  private final ToolPermissionPolicy permissionPolicy;
  private final JsonSchemaValidator schemaValidator;

  /**
   * Creates gateway.
   *
   * @param toolRegistry tool registry
   * @param permissionPolicy permission policy
   */
  public DefaultToolGateway(ToolRegistry toolRegistry, ToolPermissionPolicy permissionPolicy) {
    this(toolRegistry, permissionPolicy, new JsonSchemaValidator());
  }

  /**
   * Creates gateway.
   *
   * @param toolRegistry tool registry
   * @param permissionPolicy permission policy
   * @param schemaValidator schema validator
   */
  public DefaultToolGateway(
      ToolRegistry toolRegistry,
      ToolPermissionPolicy permissionPolicy,
      JsonSchemaValidator schemaValidator
  ) {
    this.toolRegistry = Objects.requireNonNull(toolRegistry, "toolRegistry must not be null");
    this.permissionPolicy = Objects.requireNonNull(permissionPolicy, "permissionPolicy must not be null");
    this.schemaValidator = Objects.requireNonNull(schemaValidator, "schemaValidator must not be null");
  }

  @Override
  public ToolCallResult execute(ToolCallRequest request) {
    Objects.requireNonNull(request, "request must not be null");

    ToolRegistration registration = toolRegistry.find(request.toolId()).orElse(null);
    if (registration == null) {
      return ToolCallResult.error(ERROR_TOOL_NOT_FOUND, "Tool not found: " + request.toolId());
    }

    ToolPermissionDecision permissionDecision =
        permissionPolicy.decide(request.toolId(), request, request.context());
    if (permissionDecision == ToolPermissionDecision.DENY) {
      return ToolCallResult.error(ERROR_PERMISSION_DENIED, "Permission denied for tool: " + request.toolId());
    }
    if (permissionDecision == ToolPermissionDecision.ASK) {
      return ToolCallResult.suspended(
          "Permission confirmation required for tool: " + request.toolId(),
          java.util.Map.of("toolId", request.toolId()));
    }

    String validationError = schemaValidator.validate(registration.definition().parametersSchema(), request.parameters());
    if (validationError != null) {
      return ToolCallResult.error(ERROR_INVALID_PARAMETERS, validationError);
    }

    try {
      ToolCallResult result = registration.handler().handle(request);
      if (result == null) {
        return ToolCallResult.error(ERROR_TOOL_EXECUTION_FAILED, "Tool returned null result");
      }
      return result;
    } catch (Exception ex) {
      return ToolCallResult.error(ERROR_TOOL_EXECUTION_FAILED, ex.getMessage());
    }
  }

  @Override
  public List<ToolDefinition> listTools() {
    return toolRegistry.list().stream().map(ToolRegistration::definition).toList();
  }
}
