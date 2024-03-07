import React, { useCallback, useContext, useEffect, useState } from 'react';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonLoading,
  IonPage,
  IonTitle,
  IonToolbar
} from '@ionic/react';
import { getLogger } from '../core';
import { ItemContext } from './ItemProvider';
import { RouteComponentProps } from 'react-router';
import { ItemProps } from './ItemProps';

const log = getLogger('ItemEdit');

interface ItemEditProps extends RouteComponentProps<{
  id?: string;
}> {}

const ItemEdit: React.FC<ItemEditProps> = ({ history, match }) => {
  const { items, saving, savingError, saveItem } = useContext(ItemContext);
  const [text, setText] = useState('');
  const [price, setPrice] = useState(0);
  const [date, setDate] = useState<Date>(new Date());
  const [inStock, setInStock] = useState(true);
  const [item, setItem] = useState<ItemProps>();
  useEffect(() => {
    log('useEffect');
    const routeId = match.params.id || '';
    const item = items?.find(it => it.id === routeId);
    setItem(item);
    if (item) {
      setText(item.text);
      setPrice(item.price);
      setDate(item.date);
      setInStock(item.inStock);
    }
  }, [match.params.id, items]);
  const handleSave = useCallback(() => {
    const editedItem = item ? { ...item, text, price, date, inStock } : { text, price, date, inStock };
    saveItem && saveItem(editedItem).then(() => history.goBack());
  }, [item, saveItem, text, price, date, inStock, history]);
  log('render');
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Edit</IonTitle>
          <IonButtons slot="end">
            <IonButton onClick={handleSave}>
              Save
            </IonButton>
          </IonButtons>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        <IonItem>
          <IonLabel position="stacked">Name:</IonLabel>
          <IonInput value={text} onIonChange={e => setText(e.detail.value || '')} />
        </IonItem>
        <IonItem>
          <IonLabel position="stacked">Price: </IonLabel>
          <IonInput value={price} onIonChange={e => setPrice(parseInt(e.detail.value || '0'))} />
        </IonItem>
        <IonItem>
          <IonLabel position="stacked">Release date: </IonLabel>
          <IonInput value={new Date(date).toLocaleString()} onIonChange={e => setDate(new Date(e.detail.value?.toLocaleString() || ''))} />
        </IonItem>
        <IonItem>
          <IonLabel position="stacked">In stock: </IonLabel>
          <IonInput value={inStock.toString()} onIonChange={e => setInStock(e.detail.value === "true")} />
        </IonItem>
        <IonLoading isOpen={saving} />
        {savingError && (
          <div>{savingError.message || 'Failed to save device'}</div>
        )}
      </IonContent>
    </IonPage>
  );
};

export default ItemEdit;
