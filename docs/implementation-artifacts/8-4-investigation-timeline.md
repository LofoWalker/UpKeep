# Story 8.4: Investigation Timeline

Status: ready-for-dev

## Story

As an **internal support user**, I want to see a timeline of events, so that I can understand what happened in a dispute.

## Acceptance Criteria

1. **Given** I am investigating a company or package, **When** I view Timeline tab, **Then** I see chronological events: budget changes, allocation finalizations, payout outcomes, claim status changes with timestamp, actor, details.

## Tasks

- [ ] Create `GetEntityTimelineUseCase`
- [ ] Query AuditEvent table filtered by entity
- [ ] Create timeline UI component
- [ ] Format events with details

## Dev Notes

### Timeline Query
```java
public List<TimelineEvent> getTimeline(String entityType, String entityId) {
    return auditEventRepository.findByTarget(entityType, entityId)
        .stream()
        .sorted(Comparator.comparing(AuditEvent::getTimestamp).reversed())
        .map(this::toTimelineEvent)
        .toList();
}

public record TimelineEvent(
    Instant timestamp,
    String eventType,
    String actorName,
    String description,
    Map<String, Object> details
) {}
```

### Timeline UI
```tsx
export function Timeline({ events }: { events: TimelineEvent[] }) {
  return (
    <div className="space-y-4">
      {events.map((event, i) => (
        <div key={i} className="flex gap-4">
          <div className="flex flex-col items-center">
            <div className="h-3 w-3 rounded-full bg-primary" />
            {i < events.length - 1 && <div className="w-0.5 flex-1 bg-border" />}
          </div>
          <div className="pb-4">
            <p className="text-sm text-muted-foreground">
              {formatDateTime(event.timestamp)} • {event.actorName}
            </p>
            <p className="font-medium">{event.description}</p>
          </div>
        </div>
      ))}
    </div>
  )
}
```

### References
- [Source: epics.md#Story-8.4]

## Dev Agent Record
### Agent Model Used
_To be filled_

