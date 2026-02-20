import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { importFromList } from './api';
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  Label,
} from '@/components/ui';
import { useToast } from '@/hooks/use-toast';
import { ClipboardPaste } from 'lucide-react';

interface PastePackagesDialogProps {
  companyId: string;
  onSuccess: () => void;
}

export function PastePackagesDialog({ companyId, onSuccess }: PastePackagesDialogProps) {
  const [open, setOpen] = useState(false);
  const [text, setText] = useState('');
  const { toast } = useToast();

  const { mutate, isPending } = useMutation({
    mutationFn: () => {
      const packageNames = text
        .split('\n')
        .map((line) => line.trim())
        .filter((line) => line.length > 0);
      return importFromList(companyId, { packageNames });
    },
    onSuccess: (result) => {
      const messages: string[] = [];
      if (result.importedCount > 0) {
        messages.push(`${result.importedCount} new`);
      }
      if (result.skippedCount > 0) {
        messages.push(`${result.skippedCount} already existed`);
      }
      if (result.invalidCount > 0) {
        messages.push(`${result.invalidCount} invalid`);
      }

      toast({
        title: 'Import complete',
        description: messages.join(', '),
      });
      setText('');
      setOpen(false);
      onSuccess();
    },
    onError: (error) => {
      toast({
        title: 'Import failed',
        description: error instanceof Error ? error.message : 'An error occurred',
        variant: 'destructive',
      });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (text.trim()) {
      mutate();
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline">
          <ClipboardPaste className="mr-2 h-4 w-4" />
          Paste package list
        </Button>
      </DialogTrigger>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Add Packages</DialogTitle>
            <DialogDescription>
              Paste one package name per line. Invalid names will be reported.
            </DialogDescription>
          </DialogHeader>
          <div className="py-4 space-y-2">
            <Label htmlFor="packages-text">Package names</Label>
            <textarea
              id="packages-text"
              className="flex min-h-[200px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              placeholder={"lodash\nexpress\nreact\n@types/node"}
              rows={10}
              value={text}
              onChange={(e) => setText(e.target.value)}
              disabled={isPending}
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)} disabled={isPending}>
              Cancel
            </Button>
            <Button type="submit" disabled={isPending || !text.trim()}>
              {isPending ? 'Importing...' : 'Import Packages'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

