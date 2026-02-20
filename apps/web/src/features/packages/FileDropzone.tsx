import { useCallback, useRef, useState } from 'react';
import { Upload } from 'lucide-react';
import { cn } from '@/lib/utils';

interface FileDropzoneProps {
  onFileAccepted: (file: File) => void;
  isLoading?: boolean;
}

export function FileDropzone({ onFileAccepted, isLoading }: FileDropzoneProps) {
  const [isDragActive, setIsDragActive] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragActive(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragActive(false);
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragActive(false);

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      const file = files[0];
      if (isAcceptedFile(file)) {
        onFileAccepted(file);
      }
    }
  }, [onFileAccepted]);

  const handleClick = useCallback(() => {
    inputRef.current?.click();
  }, []);

  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      onFileAccepted(files[0]);
    }
    if (inputRef.current) {
      inputRef.current.value = '';
    }
  }, [onFileAccepted]);

  return (
    <div
      onDragEnter={handleDragEnter}
      onDragLeave={handleDragLeave}
      onDragOver={handleDragOver}
      onDrop={handleDrop}
      onClick={handleClick}
      className={cn(
        'border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors',
        isDragActive ? 'border-primary bg-primary/5' : 'border-muted-foreground/25 hover:border-muted-foreground/50',
        isLoading && 'pointer-events-none opacity-50'
      )}
    >
      <input
        ref={inputRef}
        type="file"
        accept=".json,.lock"
        className="hidden"
        onChange={handleFileChange}
      />
      <Upload className="mx-auto h-12 w-12 text-muted-foreground" />
      <p className="mt-2 font-medium">
        {isLoading ? 'Importing...' : 'Drop your lockfile here or click to browse'}
      </p>
      <p className="text-sm text-muted-foreground">
        package-lock.json or yarn.lock
      </p>
    </div>
  );
}

function isAcceptedFile(file: File): boolean {
  return file.name === 'package-lock.json' || file.name === 'yarn.lock';
}

