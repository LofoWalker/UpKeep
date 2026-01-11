/**
 * REST adapters (Driving adapters) - HTTP entry points to the application.
 *
 * Controllers:
 * - Receive HTTP requests and validate input format
 * - Convert DTOs to domain commands/queries
 * - Call input ports (use cases)
 * - Convert domain results to HTTP responses
 *
 * They contain NO business logic.
 */
package com.upkeep.infrastructure.adapter.in.rest;

